#ifndef SINGLETHREAD_CLIENT_H
#define SINGLETHREAD_CLIENT_H

#include "ProxyCore.h"
#include "ConnectionHandler.h"
#include "http_parser.h"
#include "Server.h"
#include "Cache.h"

#define BUFFER_SIZE 4096

class Client : public ConnectionHandler
{
public:
    Client(int sock, ProxyCore *proxy_handler);
    ~Client() override;
    bool execute(int event) override;

    bool http_parse_error = false;
    static void initHTTPParser();
private:
    int sock;
    ProxyCore *core;
    http_parser parser;
    Server *end_point = nullptr;

    CacheEntry *entry = nullptr;
    std::string entry_key;

    bool closed = false;
    bool can_use_cache = false;

    size_t entry_offset = 0;

    std::string url;//current url target of http request
    std::string server_send_buffer; // used for data for not already opened server
    std::string prev_key; // temp data of key of http request
    std::string prev_value; // temp key of key of http request

    bool receiveData() override;
    bool sendData() override;

    static http_parser_settings settings;
    static int handleUrl(http_parser *parser, const char *at, size_t len);
    static bool sendFirstLine(Client *handler);
    static bool sendToServer(Client *handler, std::string &str);

    static int  handleHeaderField(http_parser *parser, const char *at, size_t len);
    static bool sendHeader(http_parser *parser, Client *handler);
    static bool prepareDataSource(http_parser *parser, Client *handler, std::string &host);

    static int handleHeaderValue(http_parser *parser, const char *at, size_t len);
    static int handleHeadersComplete(http_parser *parser);
    static int handleData(http_parser *parser, const char *at, size_t len);
};

#endif //SINGLETHREAD_CLIENT_H
