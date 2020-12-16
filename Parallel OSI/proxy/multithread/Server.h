#ifndef SINGLETHREAD_SERVER_H
#define SINGLETHREAD_SERVER_H

#include <netdb.h>
#include "Cache.h"
#include "ProxyCore.h"
#include "http_parser.h"

#define BUFFER_SIZE 4096

class Client;

class Server : public ConnectionHandler
{
public:
    Server(CacheEntry *cache_buff, ProxyCore *proxy_handler);
    ~Server() override;
    bool execute(int event) override;
    bool connectToServer(std::string &host);

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
    bool closed = false;
    std::string send_buffer;

    CacheEntry *buffer = nullptr;
    Client *start_point;

    http_parser parser;
    static http_parser_settings settings;

    ProxyCore *core;

    pthread_mutex_t server_mutex = PTHREAD_MUTEX_INITIALIZER;

    bool sendData() override;
    bool receiveData() override;

    static int tryResolveAddress(const std::string& host, addrinfo** res);

    static int handleMessageComplete(http_parser *parser);
};

#endif //SINGLETHREAD_SERVER_H
