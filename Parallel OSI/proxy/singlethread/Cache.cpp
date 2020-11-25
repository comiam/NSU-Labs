#include "Cache.h"
#include "Server.h"

Cache &Cache::getCache()
{
    static Cache cache;
    return cache;
}

bool Cache::contains(std::string &url)
{
    return cached_data.find(url) != cached_data.end();
}

CacheEntry *Cache::getEntry(std::string &url)
{
    return contains(url) ? cached_data.find(url)->second : nullptr;
}

CacheEntry *Cache::createEntry(std::string &url)
{
    CacheEntry *entry;
    try
    {
        entry = new CacheEntry(url);
    } catch (std::bad_alloc &e)
    {
        perror("Can't allocate new cache entry");
        return nullptr;
    }

    cached_data[url] = entry;

    return entry;
}

bool Cache::removeEntry(std::string &url)
{
    auto it = cached_data.find(url);
    if (it == cached_data.end())
        return false;

    delete(it->second);
    cached_data.erase(it);
    return true;
}

bool Cache::subscribeToEntry(std::string &url, int socket)
{
    auto it = cached_data.find(url);
    if (it == cached_data.end())
        return false;

    it->second->incSubs();
    it->second->getSubSet().insert(socket);

    return true;
}

bool Cache::unsubscribeToEntry(std::string &url, int socket)
{
    auto it = cached_data.find(url);
    if (it == cached_data.end())
        return false;

    if (it->second->getSubSet().find(socket) == it->second->getSubSet().end())
        return false;

    it->second->decSubs();
    it->second->getSubSet().erase(socket);

    if (it->second->isInvalid() || it->second->getSubscribers() == 0)
        removeEntry(url);

    return true;
}

void Cache::clearCache()
{
    for (auto &it : cached_data)
        delete(it.second);
    cached_data.clear();
}

Cache::~Cache()
{
    clearCache();
}

void CacheEntry::incSubs()
{
    ++subscribers;
}

void CacheEntry::decSubs()
{
    --subscribers;
}

CacheEntry::CacheEntry(std::string &url)
{
    invalid = url.substr(0, 2) != "01";
    data = new std::string();
}

CacheEntry::~CacheEntry()
{
    if(source)
        source->removeCacheEntry();
    sub_set.clear();
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

std::set<int> &CacheEntry::getSubSet()
{
    return sub_set;
}

void CacheEntry::unsetHavingSourceSocket()
{
    source = nullptr;
}

bool CacheEntry::isHavingSocketSource()
{
    return source != nullptr;
}

void CacheEntry::setHavingSourceSocket(Server *server)
{
    source = server;
}

std::string CacheEntry::getPartOfData(size_t beg, size_t length)
{
    return data->substr(beg, length);
}

void CacheEntry::appendData(char *buff, size_t length)
{
    data->append(buff, length);
}

size_t CacheEntry::getDataSize()
{
    return data->length();
}
