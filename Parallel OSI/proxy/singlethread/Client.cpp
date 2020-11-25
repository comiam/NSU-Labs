#include "Client.h"

http_parser_settings Client::settings;

Client::Client(int sock, ProxyCore *proxy)
{
    this->sock = sock;
    this->core = proxy;

    http_parser_init(&parser, HTTP_REQUEST);
    parser.data = this;
}

bool Client::execute(int event)
{
    if (closed)
        return false;

    if (event & POLLHUP || event & POLLERR)
        return false;

    if ((event & (POLLIN | POLLPRI)) && !receiveData())
        return false;

    if (event & POLLOUT && entry && !sendData())
        return false;

    return true;
}

bool Client::receiveData()
{
    char buff[BUFFER_SIZE];

    ssize_t len = recv(sock, buff, BUFFER_SIZE, 0);

    if (len < 0)
    {
        perror("[---ERROR---] Can't receive data from client");
        return false;
    } else if(!len)
        return false;
    else
    {
        if(end_point)
            printf("[CLIENT-RECV] Recv %zi bytes from client socket %i for server socket %i..\n", len, sock, end_point->getSocket());
        else
            printf("[CLIENT-RECV] Recv %zi bytes from client socket %i for unreleased yet server..\n", len, sock);
    }

    if (http_parser_execute(&parser, &Client::settings, buff, len) != len || http_parse_error)
    {
        perror("[---ERROR---] HTTP parsing ended with error");
        return false;
    }

    return true;
}

bool Client::sendData()
{
    std::string str = entry->getPartOfData(entry_offset, BUFFER_SIZE);
    if (!str.length())
    {
        if (entry->isFinished())
            return false;
        else if(!entry->isHavingSocketSource())
        {
            fprintf(stderr, "Can't download last part of cache %s. Downloading server socket is died!\n", url.c_str());
            return false;
        }

        core->setSocketUnavailableToSend(sock);
        return true;
    }

    entry_offset += str.length();
    if (entry_offset == entry->getDataSize() && entry->isFinished())
        closed = true;

    ssize_t len = send(sock, str.c_str(), str.length(), 0);

    if (len < 0)
        perror("[---ERROR---] Can't send data to client");
    else
    {
        if(end_point)
            printf("[CLIENT-SEND] Send %zi bytes to client socket %i by server socket %i..\n", len, sock, end_point->getSocket());
        else
            printf("[CLIENT-SEND] Send %zi bytes to client socket %i by cache..\n", len, sock);
    }

    return true;
}

Client::~Client()
{
    Cache::getCache().unsubscribeToEntry(entry_key, sock);

    if(end_point)
    {
        end_point->removeStartPoint();
        end_point->closeServer();
    }
}

void Client::initHTTPParser()
{
    http_parser_settings_init(&settings);
    settings.on_url = handleUrl;
    settings.on_header_field = handleHeaderField;
    settings.on_header_value = handleHeaderValue;
    settings.on_headers_complete = handleHeadersComplete;
    settings.on_body = handleData;
}

int Client::handleUrl(http_parser *parser, const char *at, size_t len)
{
    auto *handler = (Client *) parser->data;

    /* ignore anyone another except GET, DELETE, GET, HEAD, POST, PUT method */
    if (parser->method > 5)
    {
        fprintf(stderr, "[--WARNING--] Ignore non GET, DELETE, HEAD, POST, PUT method on socket %i: %u\n", parser->method, handler->sock);
        handler->http_parse_error = true;
        return 1;
    }

    try
    {
        handler->url.append(at, len);
    } catch (std::bad_alloc &e)
    {
        perror("[---ERROR---] Can't save client url");
        handler->http_parse_error = true;
        return 1;
    }

    return !sendFirstLine(handler);
}

int Client::handleHeaderField(http_parser *parser, const char *at, size_t len)
{
    auto *handler = (Client *) parser->data;

    if (!sendHeader(parser, handler))
        return 1;

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

int Client::handleHeaderValue(http_parser *parser, const char *at, size_t len)
{
    auto *handler = (Client *) parser->data;

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

int Client::handleHeadersComplete(http_parser *parser)
{
    auto *handler = (Client *) parser->data;
    if (!sendHeader(parser, handler))
        return 1;

    std::string end("\r\n");
    if (!sendToServer(handler, end))
        return 1;

    return 0;
}

int Client::handleData(http_parser *parser, const char *at, size_t len)
{
    auto *handler = (Client *) parser->data;
    if (handler->can_use_cache)
        return 0;

    try
    {
        if (handler->end_point)
        {
            handler->end_point->send_buffer.append(at, len);
            handler->core->setSocketAvailableToSend(handler->end_point->getSocket());
        } else
            handler->server_send_buffer.append(at, len);
    } catch (std::bad_alloc &e)
    {
        perror("[---ERROR---] Can't save data to server send buffer");
        handler->http_parse_error = true;
        return 1;
    }
    return 0;
}

bool Client::sendFirstLine(Client *handler)
{
    /* rebuild message to HTTP 1.0 protocol for best compatibility */
    printf("[CLIENT-INFO] Try get %s by socket %i\n", handler->url.c_str(), handler->sock);
    std::string line = "GET " + handler->url + " HTTP/1.0\r\n";
    return sendToServer(handler, line);
}

bool Client::sendToServer(Client *handler, std::string &str)
{
    if (handler->can_use_cache)
        return true;

    try
    {
        if (handler->end_point)
        {
            handler->end_point->send_buffer.append(str);
            handler->core->setSocketAvailableToSend(handler->end_point->getSocket());
        } else
        {
            handler->server_send_buffer.append(str);
        }
    } catch (std::bad_alloc &e)
    {
        perror("[---ERROR---] Can't save data to server send buffer");
        handler->http_parse_error = true;
        return false;
    }
    return true;
}

bool Client::sendHeader(http_parser *parser, Client *handler)
{
    if (handler->prev_key.empty())
        return true;

    /* disable Keep Alive connection */
    if (handler->prev_key == "Connection")
        handler->prev_value = "close";

    /* If we get hostname and may select data source */
    if (handler->prev_key == "Host" &&
        !prepareDataSource(parser, handler, handler->prev_value))
        return false;

    std::string header_line = handler->prev_key + ": " + handler->prev_value + "\r\n";
    handler->prev_key.clear();
    handler->prev_value.clear();
    return sendToServer(handler, header_line);
}

bool Client::prepareDataSource(http_parser *parser, Client *handler, std::string &host)
{
    char method[3];
    method[0] = parser->method / 10 + '0';
    method[1] = parser->method % 10 + '0';
    method[2] = '\0';

    std::string entry_key;
    if (handler->url[0] == '/')
        entry_key = std::string(method) + ":" + host + handler->url;
    else
        entry_key = std::string(method) + ":" + handler->url;

    Cache &cached = Cache::getCache();
    if (cached.contains(entry_key))
    {
        printf("[PROXY--INFO] Found cache of %s!\n", handler->url.c_str());

        handler->entry = cached.getEntry(entry_key);
        handler->core->setSocketAvailableToSend(handler->sock);

        if (handler->entry->isFinished())
            printf("[PROXY--INFO] This cache is full.\n");
        else
            printf("[PROXY--INFO] Use part of cache and download remaining part from %s.\n", entry_key.substr(3).c_str());

        handler->can_use_cache = true;
    } else
    {
        handler->entry = cached.createEntry(entry_key);

        Server *server;
        try
        {
            server = new Server(handler->entry, handler->core);
        } catch (std::bad_alloc &e)
        {
            printf("[PROXY-ERROR] Can't allocate new memory for server side!\n");
            return false;
        }
        server->setStartPoint(handler);

        printf("[PROXY--INFO] Cache of %s not found: connecting to %s...\n", handler->url.c_str(), host.c_str());
        if (!server->connectToServer(host))
        {
            printf("[PROXY-ERROR] Can't connect to server %s!\n", handler->url.c_str());
            handler->http_parse_error = true;
            cached.removeEntry(entry_key);

            delete(server);
            return false;
        }

        int serv = server->getSocket();
        try
        {
            server->send_buffer.append(handler->server_send_buffer);
        } catch (std::bad_alloc &e)
        {
            fprintf(stderr,"[PROXY-ERROR] Can't transfer send buffer from client socket %i to server socket %i\n", handler->sock, serv);
            return false;
        }

        handler->server_send_buffer.clear();
        handler->end_point = server;
    }
    cached.subscribeToEntry(entry_key, handler->sock);
    handler->entry_key = entry_key;

    return true;
}

void Client::removeEndPoint()
{
    this->end_point = nullptr;
}

int Client::getSock()
{
    return sock;
}

bool Client::setEndPoint(Server *_end_point)
{
    try
    {
        _end_point->send_buffer.append(this->server_send_buffer);
    } catch (std::bad_alloc &e)
    {
        fprintf(stderr,"[PROXY-ERROR] Can't transfer send buffer from NEW client socket %i to server socket %i\n", this->sock, _end_point->getSocket());
        return false;
    }
    this->server_send_buffer.clear();
    this->end_point = _end_point;

    return true;
}
