#include "Cache.h"

Cache &Cache::getCache()
{
    static Cache cache;
    return cache;
}

bool Cache::contains(std::string &url)
{
    auto it = cachedData.find(url);
    if (it == cachedData.end())
        return false;

    return true;
}

CacheEntry *Cache::getEntry(std::string &url)
{
    auto it = cachedData.find(url);
    if (it == cachedData.end())
        return nullptr;

    return it->second;
}

CacheEntry *Cache::createEntry(std::string &url)
{
    auto *entry = (CacheEntry *) malloc(sizeof(CacheEntry));
    if (!entry)
    {
        perror("Couldn't allocate new cache entry");
        return nullptr;
    }

    if (url.substr(0, 2) != "01")
        entry->invalid = true;
    else
        entry->invalid = false;

    entry->finished = false;
    entry->subscribers = 0;
    entry->sub_set = new std::set<int>();
    entry->data = new std::string();
    cachedData[url] = entry;

    return entry;
}

bool Cache::removeEntry(std::string &url)
{
    auto it = cachedData.find(url);
    if (it == cachedData.end())
        return false;

    delete it->second->sub_set;
    delete it->second->data;

    free(it->second);
    cachedData.erase(it);
    return true;
}

bool Cache::subscribeToEntry(std::string &url, int socket)
{
    auto it = cachedData.find(url);
    if (it == cachedData.end())
        return false;

    it->second->subscribers++;
    it->second->sub_set->insert(socket);

    return true;
}

bool Cache::unsubscribeToEntry(std::string &url, int socket)
{
    auto it = cachedData.find(url);
    if (it == cachedData.end())
        return false;

    if (it->second->sub_set->find(socket) == it->second->sub_set->end())
        return false;

    it->second->subscribers--;
    it->second->sub_set->erase(socket);

    if (it->second->invalid || it->second->subscribers == 0)
        removeEntry(url);

    return true;
}

void Cache::clearCache()
{
    for (auto &it : cachedData)
    {
        delete it.second->sub_set;
        delete it.second->data;
        free(it.second);
    }
    cachedData.clear();
}

Cache::~Cache()
{
    clearCache();
}