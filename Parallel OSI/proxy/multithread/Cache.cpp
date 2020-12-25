#include "Cache.h"
#include "Server.h"
#include "Client.h"

Cache &Cache::getCache()
{
    static Cache cache;
    return cache;
}

CacheEntry *Cache::subscribeToEntry(std::string &url, int client_socket)
{
    cached_data.lock();

    CacheEntry *cache;
    bool flag = cached_data.count(url) > 0;
    if(flag)
        cache = cached_data[url];
    else
        cache = createEntry(url);

    cache->lock();
    if(flag)
        cache->is_new_entry = false;
    cache->addSubToList(client_socket);
    cache->unlock();

    cached_data.unlock();
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

void Cache::unsubscribeToEntry(std::string &url, int socket)
{
    cached_data.lock();

    auto it = cached_data.find(url);
    if (it == cached_data.end())
    {
        cached_data.unlock();
        return;
    }

    it->second->lock();
    if (it->second->containsSub(socket))
    {
        it->second->unlock();
        cached_data.unlock();
        return;
    }

    it->second->removeSubFromList(socket);

    if (it->second->isInvalid() || it->second->getSubscribers() == 0)
    {
        auto *source = it->second->source;
        if(source)
        {
           source->lock();
           source->removeCacheEntry();
           source->unlock();
        }
        it->second->unlock();
        delete(it->second);
        cached_data.erase(it);
    }else
        it->second->unlock();

    cached_data.unlock();
}

Cache::~Cache()
{
    cached_data.lock();
    for (auto &it : cached_data)
        delete(it.second);
    cached_data.clear();
    cached_data.unlock();
}

CacheEntry::CacheEntry(std::string &url): Monitor()
{
    invalid = url.substr(0, 2) != "01";
    data = new std::string();
}

CacheEntry::~CacheEntry()
{
    delete data;

    sub_set.clear();
}

void CacheEntry::setFinished(bool _finished)
{
    this->finished = _finished;
}

void CacheEntry::setInvalid(bool _invalid)
{
    this->invalid = _invalid;
}

bool CacheEntry::isFinished() const
{
    return finished;
}

bool CacheEntry::isInvalid() const
{
    return invalid;
}

size_t CacheEntry::getSubscribers() const
{
    return subscribers;
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

bool CacheEntry::isCreatedNow() const
{
    return is_new_entry;
}

void CacheEntry::addSubToList(int sock)
{
    ++subscribers;
    sub_set.insert(sock);
}

void CacheEntry::noticeClientsToReadCache()
{
    for (int it : sub_set)
        source->getCore()->setSocketAvailableToSend(it);
}

void CacheEntry::removeSubFromList(int sock)
{
    --subscribers;
    sub_set.erase(sock);
}

bool CacheEntry::containsSub(int sock)
{
    return sub_set.find(sock) == sub_set.end();
}

Client *CacheEntry::getNewClientSide()
{
    Client* client = nullptr;

    for(auto sub_sock : sub_set)
    {
        client = dynamic_cast<Client*>(source->getCore()->getHandlerBySocket(sub_sock));
        if(client)
        {
            client->lock();
            if(!client->setEndPoint(source))
            {
                client->unlock();
                client = nullptr;
                continue;
            }else
            {
                client->unlock();
                break;
            }
        }
    }

    return client;
}