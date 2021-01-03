#include "Server.h"
#include "Client.h"

http_parser_settings Server::settings;

Server::Server(CacheEntry *cache_buff, ProxyCore *proxy_handler)
{
    this->entry = cache_buff;
    this->core = proxy_handler;

    cache_buff->setHavingSourceSocket(this);

    http_parser_init(&parser, HTTP_RESPONSE);
    parser.data = this;
}

bool Server::execute(int event)
{
    if (event & POLLHUP || event & POLLERR)
    {
        noticeClientAndCache();
        return false;
    }

    closed_lock.lock();
    entry_lock.lock();
    if (closed && entry)
    {
        closed_lock.unlock();
        entry->lock();
        if (entry->getSubscribers() > 0 && !entry->isFinished() && !entry->isTimedOut())
        {
            printf("[SERVER-INFO] Server socket %i lost client side socket and begin finding new client...\n", sock);

            Client *client = entry->getNewClientSide();
            entry->unlock();

            if (client)
            {
                setStartPoint(client);
                printf("[SERVER-INFO] Server socket %i became the new end point of client socket %i.\n", sock,
                       client->getSocket());
            } else
                fprintf(stdout,
                        "[SERVER-INFO] Can't find new client... "
                        "Server socket %i became work as daemon until full downloading a cache...\n", sock);

            closed_lock.lock();
            closed = false;
            closed_lock.unlock();
        } else
            entry->unlock();
    } else if (closed)
    {
        entry_lock.unlock();
        closed_lock.unlock();
        printf("[SERVER-INFO] Server socket %i lost start point and closing now...\n", sock);

        noticeClientAndCache();
        return false;
    } else if (!entry)
    {
        entry_lock.unlock();
        closed_lock.unlock();
        printf("[SERVER-INFO] Server socket %i lost its own cache entry and closing...\n", sock);

        noticeClientAndCache();
        return false;
    } else
        closed_lock.unlock();

    if ((event & (POLLIN | POLLPRI)) && !receiveData())
    {
        entry_lock.unlock();
        noticeClientAndCache();
        return false;
    } else
        entry_lock.unlock();

    io_lock.lock();
    if (!send_buffer.empty() && (event & POLLOUT) && !sendData())
    {
        io_lock.unlock();
        noticeClientAndCache();
        return false;
    }
    io_lock.unlock();

    return true;
}

bool Server::connectToServer(const std::string &host)
{
    std::string host_name = host;
    size_t split_pos = host.find_first_of(':');
    std::string port = "80";

    if (split_pos != std::string::npos)
    {
        port = host.substr(split_pos + 1);
        host_name.erase(split_pos);
    }

    if (port == "443")
    {
        fprintf(stderr, "[--WARNING--] Ignore https protocol on %s!\n", host.c_str());
        return false;
    }

    if (port != "80")
    {
        fprintf(stderr, "[---ERROR---] This port of host %s does not supported: %s!\nUse port 80!", host.c_str(),
                port.c_str());
        return false;
    }

    addrinfo *res_info;
    int res = tryResolveAddress(host_name, &res_info);
    if (res)
    {
        fprintf(stderr, "[---ERROR---] Can't resolve %s:80 %s\n", host.c_str(), gai_strerror(res));
        return false;
    }

    sock = socket(res_info->ai_family, res_info->ai_socktype, res_info->ai_protocol);
    if (sock < 0)
    {
        perror("[---ERROR---] Can't create server side socket");
        freeaddrinfo(res_info);
        return false;
    }

    if (timeoutConnect(sock, res_info) < 0)
    {
        perror("[---ERROR---] Can't connect to server");

        ProxyCore::closeSocket(sock, false);
        sock = -1;
        freeaddrinfo(res_info);
        return false;
    }

    if (!core->addSocketToPollQueue(sock, this))
    {
        perror("[---ERROR---] Can't save server socket\n");

        ProxyCore::closeSocket(sock, false);
        sock = -1;
        freeaddrinfo(res_info);
        return false;
    }

    printf("[SERVER-INFO] Connected to %s:80 on socket: %d\n", host.c_str(), sock);
    freeaddrinfo(res_info);
    return true;
}

bool Server::sendData()
{
    ssize_t len = send(sock, send_buffer.data(), send_buffer.size(), 0);

    if (len == -1)
    {
        perror("[---ERROR---] Can't send data to server");
        return false;
    } else
    {
        sp_lock.lock();
        if (start_point)
            printf("[SERVER-SEND] Send %zi bytes to server socket %i by client socket %i.\n", len, sock,
                   start_point_sock);
        else
            printf("[SERVER-SEND] Send %zi bytes to server socket %i by already closed client socket.\n", len, sock);
        sp_lock.unlock();
    }

    send_buffer.erase(send_buffer.begin(), send_buffer.begin() + len);
    if (send_buffer.empty())
        core->setSocketUnavailableToSend(sock);

    return true;
}

bool Server::receiveData()
{
    char buff[BUFFER_SIZE];

    ssize_t len = recv(sock, buff, BUFFER_SIZE, 0);

    if (len < 0)
    {
        perror("[---ERROR---] Can't recv data from server");
        if (entry)
        {
            entry->lock();
            entry->setFinished(true);
            entry->setInvalid(true);
            entry->unlock();
        }
        return false;
    } else if (!len)
    {
        if (entry)
        {
            entry->lock();
            entry->setFinished(true);
            entry->unlock();
        }
        return false;
    } else
    {
        sp_lock.lock();
        if (start_point)
            printf("[SERVER-RECV] Recv %zi bytes from server socket %i for client socket %i.\n", len, sock,
                   start_point_sock);
        else
            printf("[SERVER-RECV] Recv %zi bytes from server socket %i to cache.\n", len, sock);
        sp_lock.unlock();
    }

    size_t parsed = http_parser_execute(&parser, &Server::settings, buff, len);
    if ((ssize_t) parsed != len || http_parse_error)
    {
        perror("[---ERROR---] Can't parse http from client");
        return false;
    }

    entry->lock();
    if(cache_input_data_size != -1)
    {
        entry->setDataCapacity(cache_input_data_size);
        cache_input_data_size = -1;
    }

    try
    {
        entry->appendData(buff, len);
    } catch (std::bad_alloc &e)
    {
        perror("[---ERROR---] Can't cache server data");
        entry->unlock();
        return false;
    }
    entry->noticeClientsToReadCache();

    if(entry->isInvalid())
    {
        entry->unlock();
        return false;
    }else
        entry->unlock();

    return true;
}

int Server::getSocket() const
{
    return sock;
}

int Server::tryResolveAddress(const std::string &host, addrinfo **res)
{
    addrinfo hints{};
    memset(&hints, 0, sizeof(struct addrinfo));
    hints.ai_family = PF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;

    return getaddrinfo(host.c_str(), "80", &hints, res);
}

void Server::initHTTPParser()
{
    http_parser_settings_init(&settings);
    settings.on_message_complete    = handleMessageComplete;
    settings.on_header_field        = handleHeaderField;
    settings.on_header_value        = handleHeaderValue;
}

int Server::handleMessageComplete(http_parser *parser)
{
    auto *handler = (Server *) parser->data;

    handler->entry->lock();
    handler->entry->setFinished(true);
    if (parser->status_code != 200u)
        handler->entry->setInvalid(true);

    handler->entry->unlock();

    return 0;
}

void Server::closeServer()
{
    closed_lock.lock();
    closed = true;
    closed_lock.unlock();
}

void Server::setStartPoint(Client *client)
{
    sp_lock.lock();
    this->start_point = client;
    this->start_point_sock = start_point->getSocket();
    sp_lock.unlock();
}

void Server::removeStartPoint()
{
    sp_lock.lock();
    this->start_point = nullptr;
    sp_lock.unlock();
}

void Server::removeCacheEntry()
{
    entry_lock.lock();
    entry = nullptr;
    entry_lock.unlock();
}

ProxyCore *Server::getCore()
{
    return core;
}

void Server::putDataToSendBuffer(const char *data, size_t size)
{
    io_lock.lock();
    send_buffer.insert(send_buffer.end(), data, data + size);
    io_lock.unlock();
}

int Server::timeoutConnect(int sock, addrinfo *res_info)
{
    int arg;
    int res;
    fd_set myset;

    arg = O_NONBLOCK;
    if (fcntl(sock, F_SETFL, arg) < 0)
    {
        fprintf(stderr, "[---ERROR---] Error fcntl(..., F_SETFL) (%s)\n", strerror(errno));
        return -1;
    }

    // Trying to connect with timeout
    res = connect(sock, res_info->ai_addr, res_info->ai_addrlen);

    struct timeval tv{};

    if (res < 0)
    {
        if (errno == EINPROGRESS)
        {
            tv.tv_sec = 5;
            tv.tv_usec = 0;
            FD_ZERO(&myset);
            FD_SET(sock, &myset);
            res = select(sock + 1, nullptr, &myset, nullptr, &tv);

            if (res < 0 && errno != EINTR)
            {
                fprintf(stderr, "[---ERROR---] Error connecting on socket %i %d - %s\n", sock, errno, strerror(errno));
                return -1;
            } else if (res > 0)
            {
                // Check the value returned...
                if (!FD_ISSET(sock, &myset))
                {
                    fprintf(stderr, "[---ERROR---] Error on socket %i in delayed connection()\n", sock);
                    return -1;
                }
            } else
            {
                fprintf(stderr, "[---ERROR---] Connection timeout for socket %i! Closing socket\n", sock);
                return -1;
            }
        } else
        {
            fprintf(stderr, "[---ERROR---] Error connecting on socket %i %d - %s\n", sock, errno, strerror(errno));
            return -1;
        }
    }

    arg &= (~O_NONBLOCK);
    if (fcntl(sock, F_SETFL, arg) < 0)
    {
        fprintf(stderr, "[---ERROR---] Error fcntl(..., F_SETFL) (%s)\n", strerror(errno));
        return -1;
    }

    return 0;
}

void Server::noticeClientAndCache()
{
    entry_lock.lock();
    if (entry)
    {
        entry->lock();
        entry->unsetHavingSourceSocket();
        entry->unlock();
    }
    entry_lock.unlock();

    sp_lock.lock();
    if (start_point)
    {
        start_point->lock();
        start_point->removeEndPoint();
        start_point->unlock();
        start_point = nullptr;
    }
    sp_lock.unlock();
}

int Server::handleHeaderField(http_parser *parser, const char *at, size_t len)
{
    auto *handler = (Server *) parser->data;

    handleHeader(handler);

    try
    {
        handler->prev_key.append(at, len);
    } catch (std::bad_alloc &e)
    {
        perror("[---ERROR---] Can't save client header key");
        handler->http_parse_error = true;
        return 1;
    }

    return 0;
}

int Server::handleHeaderValue(http_parser *parser, const char *at, size_t len)
{
    auto *handler = (Server *) parser->data;

    try
    {
        handler->prev_value.append(at, len);
    } catch (std::bad_alloc &e)
    {
        perror("[---ERROR---] Can't save client header value");
        handler->http_parse_error = true;
        return 1;
    }
    return 0;
}

void Server::handleHeader(Server *handler)
{
    if (handler->prev_key.empty())
        return;

    if (handler->prev_key == "Content-Length")
        handler->cache_input_data_size = std::stoi(handler->prev_value);

    handler->prev_key.clear();
    handler->prev_value.clear();
}