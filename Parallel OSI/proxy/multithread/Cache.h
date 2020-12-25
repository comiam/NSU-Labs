#ifndef _CACHE_H
#define _CACHE_H

#include <cstdio>
#include <cstring>
#include <unistd.h>
#include <string>
#include <map>
#include <set>
#include "Monitor.h"

class Server;
class Client;

class CacheEntry: public Monitor
{
public:
    CacheEntry(std::string &url);
    ~CacheEntry();

    Client* getNewClientSide();
    bool isHavingSocketSource();
    bool isFinished() const;
    bool isCreatedNow() const;

    std::string getPartOfData(size_t beg, size_t length);
    void        appendData(char *buff, size_t length);
    size_t      getDataSize();
private:
    void setHavingSourceSocket(Server *server);
    void unsetHavingSourceSocket();
    void setFinished(bool _finished);
    void setInvalid(bool _invalid);

    void addSubToList(int sock);
    void removeSubFromList(int sock);
    void noticeClientsToReadCache();
    size_t getSubscribers() const;

    bool containsSub(int sock);
    bool isInvalid() const;

    Server *source = nullptr;
    bool is_new_entry = true;
    bool finished = false;
    bool invalid = false;
    size_t subscribers = 0;
    std::set<int> sub_set;
    std::string *data;

    friend class Cache;
    friend class Server;
};

class Cache
{
public:
    static Cache &getCache();

    CacheEntry *subscribeToEntry(std::string &url, int client_socket);
    void unsubscribeToEntry(std::string &url, int socket);

    ~Cache();
private:
    Cache() = default;

    CacheEntry *createEntry(std::string &url);

    class CacheEntryMap: public std::map<std::string, CacheEntry *>, public Monitor
    {
    public:
        CacheEntryMap(): std::map<std::string, CacheEntry *>(), Monitor() {}
    } cached_data;
};



#endif
