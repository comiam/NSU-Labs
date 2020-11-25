#ifndef _CACHE_H
#define _CACHE_H

#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <unistd.h>
#include <string>
#include <map>
#include <set>

class Server;

class CacheEntry
{
public:
    CacheEntry(std::string &url);
    ~CacheEntry();

    bool isHavingSocketSource();
    bool isFinished();

    std::string getPartOfData(size_t beg, size_t length);
    void        appendData(char *buff, size_t length);
    size_t      getDataSize();
private:
    void setHavingSourceSocket(Server *server);
    void unsetHavingSourceSocket();
    void setFinished(bool _finished);
    void setInvalid(bool _invalid);

    void incSubs();
    void decSubs();
    std::set<int> &getSubSet();
    size_t getSubscribers();

    bool isInvalid();

    Server *source;
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
    bool contains(std::string &url);

    CacheEntry *getEntry(std::string &url);
    CacheEntry *createEntry(std::string &url);

    bool removeEntry(std::string &url);
    bool subscribeToEntry(std::string &url, int socket);
    bool unsubscribeToEntry(std::string &url, int socket);

    void clearCache();
    ~Cache();
private:
    Cache() = default;

    std::map<std::string, CacheEntry *> cached_data;
};

#endif
