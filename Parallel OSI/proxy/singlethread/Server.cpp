#include "Server.h"

http_parser_settings Server::settings;

Server::Server(CacheEntry *cache_buff, ProxyCore *proxy_handler)
{
    this->buffer = cache_buff;
    this->core = proxy_handler;

    http_parser_init(&parser, HTTP_RESPONSE);
    parser.data = this;
}

bool Server::execute(int event)
{
    if (closed || !buffer || buffer->finished)
        return false;

    if (event & POLLHUP || event & POLLERR)
        return false;

    if ((event & (POLLIN | POLLPRI)) && !receiveData())
        return false;

    if ((event & POLLOUT) && !sendData())
        return false;

    return true;
}

bool Server::connectToServer(std::string &host)
{
    std::string host_name = host;
    size_t split_pos = host.find_first_of(':');
    std::string port = "80";
    if (split_pos != std::string::npos) {
        port = host.substr(split_pos + 1);
        host_name.erase(split_pos);
    }
    if (port == "443")
    {
        printf("Ignore https!\n");
        return false;
    }

    if (port != "80")
    {
        printf("Not support this port: %s\nUse port 80!", port.c_str());
        return false;
    }

    addrinfo *res_info;
    int res = tryResolveAddress(host_name, &res_info);
    if (res)
    {
        fprintf(stderr, "Can't resolve %s:80 %s\n", host.c_str(), gai_strerror(res));
        return false;
    }

    sock = socket(res_info->ai_family, res_info->ai_socktype, res_info->ai_protocol);
    if (sock < 0)
    {
        perror("Can't create server side socket");
        freeaddrinfo(res_info);
        return false;
    }

    if (connect(sock, res_info->ai_addr, res_info->ai_addrlen) < 0)
    {
        perror("Can't connect to server");
        close(sock);
        sock = -1;
        freeaddrinfo(res_info);
        return false;
    }

    if (fcntl(sock, F_SETFL, O_NONBLOCK) == -1)
    {
        perror("Can't set nonblock socket for server");
        freeaddrinfo(res_info);
        return false;
    }

    if (!core->addSocketToPoll(sock, POLLIN | POLLPRI | POLLOUT, this))
    {
        perror("Can't save server socket\n");
        close(sock);
        sock = -1;
        freeaddrinfo(res_info);
        return false;
    }

    printf("Connected to %s:80 on socket: %d\n", host.c_str(), sock);
    freeaddrinfo(res_info);
    return true;
}

bool Server::sendData()
{
    ssize_t len = send(sock, send_buffer.c_str(), send_buffer.length(), 0);

    if (len == -1)
    {
        perror("Can't send data to server");
        return false;
    }else
        printf("Write %zi bytes to server by socket %i.\n", len, sock);

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
        perror("Can't recv data from server");
        buffer->finished = true;
        buffer->invalid = true;
        return false;
    }else
        printf("Read %zi bytes from server by socket %i.\n", len, sock);

    if (!len)
    {
        buffer->finished = true;
        return false;
    }

    size_t parsed = http_parser_execute(&parser, &Server::settings, buff, len);
    if ((ssize_t) parsed != len)
    {
        perror("Can't parse http from client\n");
        return false;
    }

    try
    {
        buffer->data->append(buff, len);
    } catch (std::bad_alloc &e)
    {
        perror("Can't cache server data");
        return false;
    }

    for (int it : *buffer->sub_set)
        core->setSocketAvailableToSend(it);

    return true;
}

int Server::getSocket()
{
    return sock;
}

int Server::tryResolveAddress(std::string host, addrinfo** res)
{
    addrinfo hints;
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
    handler->buffer->finished = true;
    if (parser->status_code != 200u)
        handler->buffer->invalid = true;

    return 0;
}

void Server::closeServer()
{
    closed = true;
}
