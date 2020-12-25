#include "Server.h"
#include "Client.h"

http_parser_settings Server::settings;

Server::Server(CacheEntry *cache_buff, ProxyCore *proxy_handler): Monitor()
{
    this->entry = cache_buff;
    this->core = proxy_handler;

    cache_buff->setHavingSourceSocket(this);

    http_parser_init(&parser, HTTP_RESPONSE);
    parser.data = this;
}

Server::~Server()
{
    lock();
    if(entry)
    {
        entry->lock();
        entry->unsetHavingSourceSocket();
        entry->unlock();
    }

    if(start_point)
    {
        start_point->lock();
        start_point->removeEndPoint();
        start_point->unlock();
    }
    unlock();
}

bool Server::execute(int event)
{
    if (event & POLLHUP || event & POLLERR)
        return false;

    lock();
    if(closed && entry)
    {
        entry->lock();
        if(entry->getSubscribers() > 0 && !entry->isFinished())
        {
            printf("[SERVER-INFO] Server socket %i lost client side socket and begin finding new client...\n", sock);

            Client *client = entry->getNewClientSide();
            entry->unlock();

            if (!client)
            {
                fprintf(stderr,
                        "[---ERROR---] Can't find new client... Server socket %i became work as daemon until full downloading a cache...\n",
                        sock);
                unlock();
                return true;
            }else
                setStartPoint(client);

            printf("[SERVER-INFO] Server socket %i became the new end point of client socket %i.\n", sock,
                   client->getSocket());

            closed = false;
        }else
            entry->unlock();
    }else if(closed)
    {
        printf("[SERVER-INFO] Server socket %i lost start point and closing now...\n", sock);
        unlock();
        return false;
    } else if(!entry)
    {
        printf("[SERVER-INFO] Server socket %i lost its own cache entry and closing...\n", sock);
        unlock();
        return false;
    }

    if ((event & (POLLIN | POLLPRI)) && !receiveData())
    {
        unlock();
        return false;
    }

    if (!send_buffer.empty() && (event & POLLOUT) && !sendData())
    {
        unlock();
        return false;
    }

    unlock();

    return true;
}

bool Server::connectToServer(std::string host)
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
        fprintf(stderr, "[---ERROR---] This port of host %s does not supported: %s!\nUse port 80!", host.c_str(), port.c_str());
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

    if (connect(sock, res_info->ai_addr, res_info->ai_addrlen) < 0)
    {
        perror("[---ERROR---] Can't connect to server");

        ProxyCore::closeSocket(sock, false);
        sock = -1;
        freeaddrinfo(res_info);
        return false;
    }

    if (fcntl(sock, F_SETFL, O_NONBLOCK) == -1)
    {
        perror("[---ERROR---] Can't set nonblock socket for server");
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
    ssize_t len = send(sock, send_buffer.c_str(), send_buffer.length(), 0);

    if (len == -1)
    {
        perror("[---ERROR---] Can't send data to server");
        return false;
    }else
    {

        if(start_point)
            printf("[SERVER-SEND] Send %zi bytes to server socket %i by client socket %i.\n", len, sock,
                   start_point->getSocket());
        else
            printf("[SERVER-SEND] Send %zi bytes to server socket %i by already closed client socket.\n", len, sock);
    }

    send_buffer.erase(0, len);
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
        entry->lock();
        entry->setFinished(true);
        entry->setInvalid(true);
        entry->unlock();
        return false;
    }else if (!len)
    {
        entry->lock();
        entry->setFinished(true);
        entry->unlock();
        return false;
    }else
    {
        if(start_point)
            printf("[SERVER-RECV] Recv %zi bytes from server socket %i for client socket %i.\n", len, sock,
                   start_point->getSocket());
        else
            printf("[SERVER-RECV] Recv %zi bytes from server socket %i to cache.\n", len, sock);
    }

    size_t parsed = http_parser_execute(&parser, &Server::settings, buff, len);
    if ((ssize_t) parsed != len)
    {
        perror("[---ERROR---] Can't parse http from client");
        return false;
    }

    entry->lock();
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
    entry->unlock();

    return true;
}

int Server::getSocket() const
{
    return sock;
}

int Server::tryResolveAddress(const std::string& host, addrinfo** res)
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
    settings.on_message_complete = handleMessageComplete;
}

int Server::handleMessageComplete(http_parser *parser)
{
    auto *handler = (Server *)parser->data;

    handler->entry->lock();
    handler->entry->setFinished(true);
    if (parser->status_code != 200u)
        handler->entry->setInvalid(true);

    handler->entry->unlock();

    return 0;
}

void Server::closeServer()
{
    closed = true;
}

void Server::setStartPoint(Client *client)
{
    this->start_point = client;
}

void Server::removeStartPoint()
{
    this->start_point = nullptr;
}

void Server::removeCacheEntry()
{
    entry = nullptr;
}

ProxyCore *Server::getCore()
{
    return core;
}

void Server::putDataToSendBuffer(const char *data, size_t size)
{
    send_buffer.append(data, size);
}
