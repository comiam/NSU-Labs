#ifndef MULTITHREAD_CLIENT_H
#define MULTITHREAD_CLIENT_H

#include "ProxyCore.h"
#include "ConnectionHandler.h"
#include "http_parser.h"
#include "Server.h"
#include "Cache.h"

#define BUFFER_SIZE 4096 * 16

class Client : public ConnectionHandler, public Monitor
{
public:
    Client(int sock, ProxyCore *proxy_handler);
    ~Client() override = default;

    int getSocket() const;

    void removeEndPoint();
    bool setEndPoint(Server *_end_point);

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
    std::string server_send_buffer; // used for pipe_data for not already opened server
    std::string prev_key; // temp pipe_data of key of http request
    std::string prev_value; // temp key of key of http request

    std::string host_name;
    std::string range_bytes;

    bool receiveData() override;
    bool sendData() override;

    void destroyClientSide();

    static http_parser_settings settings;
    static int handleUrl(http_parser *parser, const char *at, size_t len);
    static bool sendFirstLine(Client *handler);
    static bool sendToServer(Client *handler, std::string &str);

    static int  handleHeaderField(http_parser *parser, const char *at, size_t len);
    static bool sendHeader(Client *handler);
    static bool prepareDataSource(http_parser *parser, Client *handler, std::string &host);

    static int handleHeaderValue(http_parser *parser, const char *at, size_t len);
    static int handleHeadersComplete(http_parser *parser);
    static int handleData(http_parser *parser, const char *at, size_t len);
};

#endif
