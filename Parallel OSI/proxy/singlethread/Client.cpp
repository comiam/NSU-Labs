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
        perror("Can't receive data from client");
        return false;
    } else
        printf("Receive %zi bytes from client by socket %i..\n", len, sock);

    if(!len)
        return false;

    if (http_parser_execute(&parser, &Client::settings, buff, len) != len || http_parse_error)
    {
        perror("HTTP parsing ended with error");
        return false;
    }

    return true;
}

bool Client::sendData()
{
    std::string str = entry->getData()->substr(entry_offset, BUFFER_SIZE);
    if (!str.length())
    {
        if (entry->isFinished())
            return false;

        core->setSocketUnavailableToSend(sock);
        return true;
    }

    entry_offset += str.length();
    if (entry_offset == entry->getData()->length() && entry->isFinished())
        closed = true;

    ssize_t len = send(sock, str.c_str(), str.length(), 0);

    if (len < 0)
        perror("Can't send data to client");
    else
        printf("Send %zi bytes to client by socket %i.\n", len, sock);

    return true;
}

Client::~Client()
{
    Cache::getCache().unsubscribeToEntry(entry_key, sock);
    if(!can_use_cache && end_point)
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

    /* ignore anyone another except GET method */
    if (parser->method != 1u)
    {
        printf("ignore non GET method: %u\n", parser->method);
        handler->http_parse_error = true;
        return 1;
    }

    try
    {
        handler->url.append(at, len);
    } catch (std::bad_alloc &e)
    {
        perror("Can't save client url");
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
        perror("Couldn't save client header key");
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
        perror("Can't save client header value");
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
        perror("Can't save data to server send buffer");
        handler->http_parse_error = true;
        return 1;
    }
    return 0;
}

bool Client::sendFirstLine(Client *handler)
{
    /* rebuild message to HTTP 1.0 protocol for best compatibility */
    printf("Try get %s by socket %i\n", handler->url.c_str(), handler->sock);
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
        perror("Can't save data to server send buffer");
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
        printf("Found cache of this data!\n");

        handler->entry = cached.getEntry(entry_key);
        handler->core->setSocketAvailableToSend(handler->sock);

        if (handler->entry->isFinished())
            printf("Use cache from %s.\n", entry_key.substr(3).c_str());
        else
            printf("Use part of cache and download remaining part from %s.\n", entry_key.substr(3).c_str());

        handler->can_use_cache = true;
    } else
    {
        handler->entry = cached.createEntry(entry_key);
        auto *server = new Server(handler->entry, handler->core);
        server->setStartPoint(handler);

        printf("Cache of %s not found: connecting to %s...\n", handler->url.c_str(), host.c_str());
        if (!server->connectToServer(host))
        {
            printf("Can't connect to server %s!\n", handler->url.c_str());
            handler->http_parse_error = true;
            cached.removeEntry(entry_key);
            return false;
        }

        try
        {
            server->send_buffer.append(handler->server_send_buffer);
        } catch (std::bad_alloc &e)
        {
            perror("Can't transfer send buffer from client to server");
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
