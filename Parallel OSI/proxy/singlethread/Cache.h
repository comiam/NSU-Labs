#ifndef _CACHE_H
#define _CACHE_H

#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <unistd.h>
#include <string>
#include <map>
#include <set>

class CacheEntry
{
public:
    CacheEntry(std::string &url);
    ~CacheEntry();

    void setFinished(bool _finished);
    void setInvalid(bool _invalid);
    void incSubs();
    void decSubs();

    bool isFinished();
    bool isInvalid();
    size_t getSubscribers();

    std::set<int> *getSubSet();
    std::string *getData();

private:
    bool finished = false;
    bool invalid = false;
    size_t subscribers = 0;
    std::set<int> *sub_set;
    std::string *data;
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
    Cache(Cache const &);
    void operator=(Cache const &);

    std::map<std::string, CacheEntry *> cachedData;
};

#endif
