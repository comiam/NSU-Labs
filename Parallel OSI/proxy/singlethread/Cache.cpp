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

    it->second->incSubs();
    it->second->getSubSet()->insert(socket);

    return true;
}

bool Cache::unsubscribeToEntry(std::string &url, int socket)
{
    auto it = cachedData.find(url);
    if (it == cachedData.end())
        return false;

    if (it->second->getSubSet()->find(socket) == it->second->getSubSet()->end())
        return false;

    it->second->decSubs();
    it->second->getSubSet()->erase(socket);

    if (it->second->isInvalid() || it->second->getSubscribers() == 0)
        removeEntry(url);

    return true;
}

void Cache::clearCache()
{
    for (auto &it : cachedData)
        delete(it.second);
    cachedData.clear();
}

Cache::~Cache()
{
    clearCache();
}

CacheEntry::CacheEntry(std::string &url)
{
    invalid = url.substr(0, 2) != "01";
    sub_set = new std::set<int>();
    data = new std::string();
}

CacheEntry::~CacheEntry()
{
    delete sub_set;
    delete data;
}

void CacheEntry::setFinished(bool _finished)
{
    this->finished = _finished;
}

void CacheEntry::setInvalid(bool _invalid)
{
    this->invalid = _invalid;
}

void CacheEntry::incSubs()
{
    ++subscribers;
}

void CacheEntry::decSubs()
{
    --subscribers;
}

bool CacheEntry::isFinished()
{
    return finished;
}

bool CacheEntry::isInvalid()
{
    return invalid;
}

size_t CacheEntry::getSubscribers()
{
    return subscribers;
}

std::set<int> *CacheEntry::getSubSet()
{
    return sub_set;
}

std::string *CacheEntry::getData()
{
    return data;
}
