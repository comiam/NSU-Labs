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

    int getSocket();
    std::string send_buffer;

    void setStartPoint(Client *client);
    void removeStartPoint();
    void removeCacheEntry();

    static void initHTTPParser();
    void closeServer();
private:
    int sock = -1;
    bool closed = false;


    CacheEntry *buffer = nullptr;
    Client *start_point;

    http_parser parser;
    static http_parser_settings settings;

    ProxyCore *core;

    bool sendData() override;
    bool receiveData() override;

    static int tryResolveAddress(const std::string& host, addrinfo** res);

    static int handleMessageComplete(http_parser *parser);
};

#endif //SINGLETHREAD_SERVER_H
