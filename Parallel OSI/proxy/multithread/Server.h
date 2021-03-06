#ifndef MULTITHREAD_SERVER_H
#define MULTITHREAD_SERVER_H

#include <netdb.h>
#include "Cache.h"
#include "ProxyCore.h"
#include "http_parser.h"

class Client;

class Server: public ConnectionHandler
{
public:
    Server(CacheEntry *cache_buff, ProxyCore *proxy_handler);
    ~Server() override = default;
    bool execute(int event) override;
    bool connectToServer(const std::string& host);

    int getSocket() const;

    void putDataToSendBuffer(const char* data, size_t size);
    void setStartPoint(Client *client);
    void removeStartPoint();
    void removeCacheEntry();
    ProxyCore *getCore();

    static void initHTTPParser();
    void closeServer();
private:
    int sock = -1;

    Monitor io_lock;
    std::vector<char> send_buffer;

    Monitor closed_lock;
    bool closed = false;

    Monitor entry_lock;
    CacheEntry *entry = nullptr;

    Monitor sp_lock;
    Client *start_point = nullptr;
    int start_point_sock = 0;

    http_parser parser;
    static http_parser_settings settings;

    ProxyCore *core = nullptr;

    bool http_parse_error = false;
    std::string prev_key;
    std::string prev_value;
    long cache_input_data_size = -1;

    void noticeClientAndCache();
    bool sendData() override;
    bool receiveData() override;

    static int  timeoutConnect(int sock, addrinfo *res_info);
    static int  tryResolveAddress(const std::string& host, addrinfo** res);
    static int  handleMessageComplete(http_parser *parser);

    static int  handleHeaderField(http_parser *parser, const char *at, size_t len);
    static int  handleHeaderValue(http_parser *parser, const char *at, size_t len);
    static void handleHeader(Server *handler);
};

#endif
