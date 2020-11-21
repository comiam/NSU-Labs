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
    CacheEntry *entry;
    try
    {
        entry = new CacheEntry(url);
    } catch (std::bad_alloc &e)
    {
        perror("Couldn't allocate new cache entry");
        return nullptr;
    }

    cachedData[url] = entry;

    return entry;
}

bool Cache::removeEntry(std::string &url)
{
    auto it = cachedData.find(url);
    if (it == cachedData.end())
        return false;

    delete(it->second);
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

CacheEntry::CacheEntry(std::string &url)
{
    invalid = url.substr(0, 2) != "01";
    finished = false;
    subscribers = 0;
    sub_set = new std::set<int>();
    data = new std::string();
}

CacheEntry::~CacheEntry()
{
    delete sub_set;
    delete data;
}
