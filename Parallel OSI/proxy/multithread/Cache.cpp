#include "Cache.h"
#include "Server.h"
#include "Client.h"

Cache &Cache::getCache()
{
    static Cache cache;
    return cache;
}

bool Cache::contains(std::string &url)
{
    return cached_data.find(url) != cached_data.end();
}

CacheEntry *Cache::subscribeToEntry(std::string &url, int client_socket)
{
    lock_guard lock(&access_mutex);

    CacheEntry *cache;
    if(contains(url))
    {
        cache = cached_data.find(url)->second;
        cache->is_new_entry = false;
    }else
        cache = createEntry(url);

    cache->addSubToList(client_socket);

    return cache;
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

void Cache::unsubscribeToEntry(std::string &url, int socket)
{
    lock_guard lock(&access_mutex);

    auto it = cached_data.find(url);
    if (it == cached_data.end())
        return;

    if (it->second->containsSub(socket))
        return;

    it->second->removeSubFromList(socket);

    if (it->second->isInvalid() || it->second->getSubscribers() == 0)
        removeEntry(url);
}

void Cache::clearCache()
{
    lock_guard lock(&access_mutex);

    for (auto &it : cached_data)
        delete(it.second);
    cached_data.clear();
}

Cache::~Cache()
{
    clearCache();
    pthread_mutex_destroy(&access_mutex);
}

void Cache::initAccessMutex()
{
    pthread_mutexattr_t attr;
    pthread_mutexattr_init(&attr);
    pthread_mutexattr_settype(&attr, PTHREAD_MUTEX_ERRORCHECK);

    pthread_mutex_init(&access_mutex, &attr);
}

Cache::Cache()
{
    initAccessMutex();
}

CacheEntry::CacheEntry(std::string &url)
{
    invalid = url.substr(0, 2) != "01";
    data = new std::string();
    initAccessMutex();
}

CacheEntry::~CacheEntry()
{
    pthread_mutex_lock(&entry_mutex);
    pthread_mutex_lock(&sub_mutex);
    if(source)
        source->removeCacheEntry();
    delete data;

    sub_set.clear();
    pthread_mutex_unlock(&sub_mutex);
    pthread_mutex_unlock(&entry_mutex);

    pthread_mutex_destroy(&sub_mutex);
    pthread_mutex_destroy(&entry_mutex);
}

void CacheEntry::setFinished(bool _finished)
{
    lock_guard lock(&entry_mutex);
    this->finished = _finished;
}

void CacheEntry::setInvalid(bool _invalid)
{
    lock_guard lock(&entry_mutex);
    this->invalid = _invalid;
}

bool CacheEntry::isFinished()
{
    lock_guard lock(&entry_mutex);
    return finished;
}

bool CacheEntry::isInvalid()
{
    lock_guard lock(&entry_mutex);
    return invalid;
}

size_t CacheEntry::getSubscribers()
{
    lock_guard lock(&entry_mutex);
    return subscribers;
}

void CacheEntry::unsetHavingSourceSocket()
{
    lock_guard lock(&entry_mutex);
    source = nullptr;
}

bool CacheEntry::isHavingSocketSource()
{
    lock_guard lock(&entry_mutex);
    return source != nullptr;
}

void CacheEntry::setHavingSourceSocket(Server *server)
{
    lock_guard lock(&entry_mutex);
    source = server;
}

std::string CacheEntry::getPartOfData(size_t beg, size_t length)
{
    lock_guard lock(&entry_mutex);
    return data->substr(beg, length);
}

void CacheEntry::appendData(char *buff, size_t length)
{
    lock_guard lock(&entry_mutex);
    data->append(buff, length);
}

size_t CacheEntry::getDataSize()
{
    lock_guard lock(&entry_mutex);
    return data->length();
}

bool CacheEntry::isCreatedNow()
{
    lock_guard lock(&entry_mutex);
    return is_new_entry;
}

void CacheEntry::initAccessMutex()
{
    pthread_mutexattr_t attr;
    pthread_mutexattr_init(&attr);
    pthread_mutexattr_settype(&attr, PTHREAD_MUTEX_ERRORCHECK);

    pthread_mutex_init(&entry_mutex, &attr);
    pthread_mutex_init(&sub_mutex, &attr);
}

void CacheEntry::addSubToList(int sock)
{
    lock_guard lock(&sub_mutex);
    ++subscribers;
    sub_set.insert(sock);
}

void CacheEntry::noticeClientsToReadCache()
{
    lock_guard lock(&entry_mutex);
    for (int it : sub_set)
        source->getCore()->setSocketAvailableToSend(it);
}

void CacheEntry::removeSubFromList(int sock)
{
    lock_guard lock(&sub_mutex);
    --subscribers;
    sub_set.erase(sock);
}

bool CacheEntry::containsSub(int sock)
{
    lock_guard lock(&sub_mutex);
    return sub_set.find(sock) == sub_set.end();
}

Client *CacheEntry::getNewClientSide()
{
    lock_guard lock(&sub_mutex);
    Client* client = nullptr;

    for(auto sub_sock : sub_set)
    {
        client = dynamic_cast<Client*>(source->getCore()->getHandlerBySocket(sub_sock));
        if(client && !client->setEndPoint(source))
        {
            client = nullptr;
            continue;
        }else if(client)
            break;
    }

    return client;
}