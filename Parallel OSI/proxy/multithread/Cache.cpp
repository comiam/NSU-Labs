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
    cache->resetTimer();

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
    if (!it->second->containsSub(socket))
    {
        it->second->unlock();
        cached_data.unlock();
        return;
    }

    it->second->removeSubFromList(socket);

    if (it->second->isInvalid() || (it->second->getSubscribers() == 0 && it->second->isTimedOut()))
    {
        auto *source = it->second->source;
        it->second->unlock();
        if(source)
            source->removeCacheEntry();

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

void Cache::updateTimers()
{
    cached_data.lock();

    struct timespec newTime{0, 0};
    clock_gettime (CLOCK_REALTIME, &newTime);

    for(auto set = cached_data.begin();set != cached_data.end();)
    {
        CacheEntry* entry = set->second;
        entry->lock();

        entry->updateTimer(newTime);
        if(entry->isTimedOut() && entry->getSubscribers() == 0)
        {
            auto *source = entry->source;
            entry->unlock();
            if(source)
                source->removeCacheEntry();

            delete(entry);
            set = cached_data.erase(set);
        }else
        {
            entry->unlock();
            ++set;
        }
    }

    cached_data.unlock();
}

CacheEntry::CacheEntry(std::string &url): Monitor()
{
    invalid = url.substr(0, 2) != "01";
    this->url = url;
    data = new std::string();
}

CacheEntry::~CacheEntry()
{
    delete data;
    printf("[PROXY--CORE] Cache of %s is deleted from memory...\n", url.substr(3, url.size()).c_str());

    sub_set.clear();
    url.clear();
}

void CacheEntry::setFinished(bool finished)
{
    this->finished = finished;
}

void CacheEntry::setInvalid(bool invalid)
{
    this->invalid = invalid;
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
    resetTimer();
    return data->substr(beg, length);
}

void CacheEntry::appendData(char *buff, size_t length)
{
    data->append(buff, length);
}

size_t CacheEntry::getDataSize()
{
    resetTimer();
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

void CacheEntry::resetTimer()
{
    live_time_total = 0;
}

bool CacheEntry::isTimedOut() const
{
    return live_time_total >= MAX_LIVE_TIME_NANO;
}

void CacheEntry::updateTimer(struct timespec &newTime)
{
    live_time_total += NANO * (newTime.tv_sec - live_time_elapsed.tv_sec) + (newTime.tv_nsec - live_time_elapsed.tv_nsec);
    live_time_elapsed.tv_nsec = newTime.tv_nsec;
    live_time_elapsed.tv_sec = newTime.tv_sec;
}
