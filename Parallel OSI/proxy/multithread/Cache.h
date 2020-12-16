#ifndef _CACHE_H
#define _CACHE_H

#include <cstdio>
#include <cstring>
#include <unistd.h>
#include <string>
#include <map>
#include <set>

#include "lock_guard.h"

class Server;
class Client;

class CacheEntry
{
public:
    CacheEntry(std::string &url);
    ~CacheEntry();

    Client* getNewClientSide();
    bool isHavingSocketSource();
    bool isFinished();
    bool isCreatedNow();

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
    size_t getSubscribers();

    bool containsSub(int sock);
    bool isInvalid();

    void initAccessMutex();

    Server *source = nullptr;
    bool is_new_entry = true;
    bool finished = false;
    bool invalid = false;
    size_t subscribers = 0;
    std::set<int> sub_set;
    std::string *data;

    pthread_mutex_t entry_mutex = PTHREAD_MUTEX_INITIALIZER;
    pthread_mutex_t sub_mutex = PTHREAD_MUTEX_INITIALIZER;

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
    Cache();
    void clearCache();
    void initAccessMutex();

    CacheEntry *createEntry(std::string &url);
    bool removeEntry(std::string &url);
    bool contains(std::string &url);

    std::map<std::string, CacheEntry *> cached_data;

    pthread_mutex_t access_mutex = PTHREAD_MUTEX_INITIALIZER;
};

#endif
