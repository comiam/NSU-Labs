#ifndef MULTITHREAD_SERVER_H
#define MULTITHREAD_SERVER_H

#include <netdb.h>
#include "Cache.h"
#include "ProxyCore.h"
#include "http_parser.h"

#define BUFFER_SIZE 4096 * 16

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
    std::string send_buffer;

    Monitor closed_lock;
    bool closed = false;

    Monitor entry_lock;
    CacheEntry *entry = nullptr;

    Monitor sp_lock;
    Client *start_point = nullptr;

    http_parser parser;
    static http_parser_settings settings;

    ProxyCore *core = nullptr;

    void noticeClientAndCache();
    bool sendData() override;
    bool receiveData() override;

    static int timeoutConnect(int sock, addrinfo *res_info);
    static int tryResolveAddress(const std::string& host, addrinfo** res);
    static int handleMessageComplete(http_parser *parser);
};

#endif
