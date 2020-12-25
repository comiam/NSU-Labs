#include "ProxyCore.h"
#include "Client.h"

void *routine(void* args);

void *worker_routine(void *args);

pthread_t initProxyCore(std::pair<int, int> &args)
{
    pthread_t proxy_thread;
    if (!pthread_create(&proxy_thread, nullptr, routine, &args))
    {
        printf("Proxy thread created!\n");
        return proxy_thread;
    }
    else
    {
        fprintf(stderr, "Can't create proxy thread! Closing...\n");
        return 0;
    }
}

ProxyCore::ProxyCore(int port, int thread_count)
{
    sockaddr_in addr{};
    addr.sin_family = AF_INET;
    addr.sin_port = htons(port);
    addr.sin_addr.s_addr = htonl(INADDR_ANY);

    proxy_socket = socket(AF_INET, SOCK_STREAM, 0);

    if(proxy_socket == -1)
    {
        perror("[PROXY-ERROR] Can't create server socket");
        return;
    }

    if (bind(proxy_socket, (sockaddr *) &addr, sizeof(addr)) == -1)
    {
        perror("[PROXY-ERROR] Can't bind socket on port");
        close(proxy_socket);
        return;
    }

    if(!initSocket(proxy_socket) || !initPollSet())
        return;

    Client::initHTTPParser();
    Server::initHTTPParser();

    this->port = port;

    pthread_t thread;
    for (int i = 0; i < thread_count; ++i)
    {
        if(pthread_create(&thread, nullptr, worker_routine, (void*)this))
        {
            printf("Can't create needed count of worker threads for proxy! Shutting down...\n");

            for (auto j : thread_pool)
                pthread_cancel(j);
            return;
        }
        thread_pool.push_back(thread);
    }

    printf("\n[PROXY--CORE] Proxy created!\n");

    created = true;
}

ProxyCore::~ProxyCore()
{
    clearData();
    printf("\n[PROXY--CORE] Proxy closed!\n");

    created = false;
    closing = true;
}

void ProxyCore::clearData()
{
    closing = true;

#ifdef DEBUG_ENABLED
    printf("Closing childs...\n");
#endif
    task_list.lock();
    task_list.notifyAll();
    task_list.unlock();

    for(auto &thr : thread_pool)
        pthread_join(thr, nullptr);
#ifdef DEBUG_ENABLED
    printf("End closing childs...\n");
    printf("Closing sockets...\n");
#endif

    for (auto & i : poll_set)
    {
        closeSocket(i.fd, i.fd == proxy_socket);

        if (socketHandlers[i.fd])
            delete(socketHandlers[i.fd]);
    }
#ifdef DEBUG_ENABLED
    printf("Poll set of sockets closed...\n");
    printf("Begin close busy set of sockets...\n");
#endif
    for (auto & i : busy_set)
    {
        closeSocket(i.first, false);

        if (socketHandlers[i.first])
            delete(socketHandlers[i.first]);
    }
#ifdef DEBUG_ENABLED
    printf("End closing sockets...\n");
    printf("Clear data and closing monitors...\n");
#endif

    thread_pool.clear();
    socketHandlers.clear();
    new_server_set.clear();
    sock_rdwr.clear();
    task_list.clear();
    trash_set.clear();
    free_set.clear();
    poll_set.clear();
    busy_set.clear();

    unlockMonitor(add_lock);
    unlockMonitor(free_lock);
    unlockMonitor(remove_lock);
    unlockMonitor(rdwr_lock);
}

bool ProxyCore::listenConnections()
{
    int count;
    int revent;
    int new_client;

    printf("[PROXY--CORE] Proxy started on port: %d\n", port);

    std::vector<pollfd> trashbox(0);

    while (true)
    {
        for(auto &i : trashbox)
            poll_set.erase(std::remove(poll_set.begin(), poll_set.end(), i), poll_set.end());
        trashbox.clear();

        rdwr_lock.lock();
        for (auto &i : sock_rdwr)
        {
            ssize_t pos = getSocketIndex(i.first);
            if (pos != -1)
            {
                if(i.second)
                    poll_set[pos].events |= POLLOUT;
                else
                    poll_set[pos].events &= ~POLLOUT;
            }
        }
        sock_rdwr.clear();
        rdwr_lock.unlock();

        add_lock.lock();
        for (auto &i : new_server_set)
            if (!addSocketToPollWithoutBlocking(i.first, POLLIN | POLLPRI | POLLOUT, i.second))
            {
                fprintf(stderr, "Can't add new server socket to poll from add set! Closing...");
                closeSocket(new_client, false);
                clearData();
                return false;
            }
        new_server_set.clear();
        add_lock.unlock();

        free_lock.lock();
        for (auto &i : free_set)
        {
            poll_set.push_back(busy_set[i]);
            busy_set.erase(i);
        }
        free_set.clear();
        free_lock.unlock();

        remove_lock.lock();
        for (auto &i : trash_set)
        {
            removeHandlerImpl(i);
            busy_set.erase(i);
        }
        trash_set.clear();
        remove_lock.unlock();

        count = poll(poll_set.data(), (nfds_t) poll_set.size(), 1);

        if (count == -1)
        {
            if (errno == EINTR)
                continue;

            fprintf(stderr, "[PROXY-ERROR] Poll error: %s\n", strerror(errno));
            break;
        }else if (count == 0)
            continue;
        else
        {
            for (size_t i = 0; i < poll_set.size(); ++i)
            {
                revent = poll_set[i].revents;
                if (!revent)
                    continue;
                else
                {
                    poll_set[i].revents = 0;

                    if (!i)
                    {
                        if (revent & (POLLIN | POLLPRI))
                        {
                            new_client = accept(proxy_socket, nullptr, nullptr);
                            if (new_client == -1)
                            {
                                perror("[PROXY-ERROR] Can't accept new client");
                                clearData();
                                return false;
                            }

                            Client *client;
                            try
                            {
                                client = new Client(new_client, this);
                            } catch (std::bad_alloc &e)
                            {
                                perror("[PROXY-ERROR] Can't allocate new client");

                                closeSocket(new_client, false);
                                clearData();
                                return false;
                            }

                            if (!addSocketToPollWithoutBlocking(new_client, POLLIN | POLLPRI, client))
                            {
                                fprintf(stderr, "Can't add new socket to poll! Closing...");
                                closeSocket(new_client, false);
                                clearData();
                                return false;
                            }

                            printf("[PROXY--INFO] Connected new user with socket %d\n", new_client);
                        } else
                        {
                            fprintf(stderr, "[PROXY-ERROR] Can't accept new client! Closing proxy...\n");
                            clearData();
                            return false;
                        }
                    } else
                    {
                        task_list.lock();
                        pollfd val = poll_set[i];
                        auto p = std::make_pair(val.fd, revent);
                        if (!task_list.count(p))
                        {
                            task_list.insert(p);
                            busy_set[val.fd] = val;
                            trashbox.push_back(val);
                        }
                        task_list.notify();
                        task_list.unlock();
                    }
                }
            }
        }
    }

    return false;
}

bool operator==(const pollfd &first, const pollfd &second)
{
    return first.fd == second.fd && first.events == second.events;
}

template<typename Base, typename T>
inline bool instanceOf(const T *ptr)
{
    return dynamic_cast<const Base*>(ptr) != nullptr;
}

bool ProxyCore::addSocketToPollWithoutBlocking(int socket, short events, ConnectionHandler *executor)
{
    pollfd fd{};
    fd.fd = socket;
    fd.events = events;
    fd.revents = 0;

    bool success = true;
    try
    {
        poll_set.push_back(fd);
        socketHandlers[socket] = executor;
    } catch (std::bad_alloc &e)
    {
        perror("[PROXY-ERROR] Can't allocate new pollfd or handler");
        success = false;
    }

    return success;
}

bool ProxyCore::addSocketToPoll(int socket, ConnectionHandler *executor)
{
    add_lock.lock();
    new_server_set.emplace_back(socket, executor);
    add_lock.unlock();

    return true;
}

bool ProxyCore::initSocket(int sock_fd)
{
    proxy_socket = sock_fd;

    if (listen(proxy_socket, POLL_SIZE_SEGMENT) == -1)
    {
        perror("[PROXY-ERROR] Can't set listen to server socket");
        clearData();
        return false;
    }

    return true;
}

ssize_t ProxyCore::getSocketIndex(int _sock)
{
    for(auto iter = poll_set.begin(); iter != poll_set.end(); ++iter)
        if((*iter).fd == _sock)
            return std::distance(poll_set.begin(), iter);

    return -1;
}

void ProxyCore::setSocketAvailableToSend(int socket)
{
    rdwr_lock.lock();
    sock_rdwr.emplace_back(socket, 1);
    rdwr_lock.unlock();
}

void ProxyCore::setSocketUnavailableToSend(int socket)
{
    rdwr_lock.lock();
    sock_rdwr.emplace_back(socket, 0);
    rdwr_lock.unlock();
}

bool ProxyCore::initPollSet()
{
    pollfd fd{};
    fd.fd = proxy_socket;
    fd.events = POLLIN | POLLPRI;
    fd.revents = 0;

    try
    {
        poll_set.emplace_back(fd);
    } catch (std::bad_alloc &e)
    {
        perror("[PROXY-ERROR] Can't allocate memory for poll set!");
        return false;
    }
    return true;
}

bool ProxyCore::isCreated() const
{
    return created;
}

ConnectionHandler *ProxyCore::getHandlerBySocket(int socket)
{
    return !socketHandlers.count(socket) ? nullptr : socketHandlers[socket];
}

void ProxyCore::closeSocket(int _sock, bool is_server_sock)
{
    shutdown(_sock, !is_server_sock ? SHUT_RDWR : SHUT_WR);
    close(_sock);
}

void ProxyCore::removeHandler(int socket)
{
    lockHandlers();
    trash_set.push_back(socket);
    unlockHandlers();
}

std::pair<int, int> ProxyCore::getTask()
{
    if(closing)
        return std::make_pair(-1,-1);

    task_list.lock();

    while(task_list.empty() && !closing)
        task_list.wait();

    if(closing)
    {
        task_list.unlock();
        return std::make_pair(-1,-1);
    }

    auto elem = *task_list.begin();
    task_list.erase(elem);
    task_list.unlock();

    return elem;
}

void ProxyCore::lockHandlers()
{
    remove_lock.lock();
}

void ProxyCore::unlockHandlers()
{
    remove_lock.unlock();
}

void ProxyCore::madeSocketFreeForPoll(int _sock)
{
    free_lock.lock();
    free_set.push_back(_sock);
    free_lock.unlock();
}

void ProxyCore::removeHandlerImpl(int _sock)
{
    if(!socketHandlers[_sock])
        return;

    printf("[PROXY--CORE] %s socket %i closed\n", instanceOf<Client>(socketHandlers[_sock]) ? "Client" : "Server", _sock);

    delete(socketHandlers[_sock]);

    socketHandlers.erase(_sock);
    for(auto iter = poll_set.begin(); iter != poll_set.end(); ++iter)
        if((*iter).fd == _sock)
        {
            poll_set.erase(iter);
            break;
        }

    closeSocket(_sock, false);
}

void ProxyCore::unlockMonitor(Monitor monitor)
{
    if(monitor.isLocked())
        monitor.unlock();
}

void cleanProxy(void* arg)
{
    delete((ProxyCore*)arg);
}

void *routine(void *args)
{
    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, nullptr);
    pthread_setcanceltype(PTHREAD_CANCEL_DEFERRED, nullptr);
    ProxyCore *proxy;

    auto *p = (std::pair<int, int>*)args;

    try
    {
        proxy = new ProxyCore(p->first, p->second);
    } catch (std::bad_alloc &e)
    {
        perror("Can't init proxy");
        return nullptr;
    }

    pthread_cleanup_push(cleanProxy, proxy);

    if(!proxy->isCreated())
    {
        perror("Can't init proxy");
        return nullptr;
    }

    if (proxy->listenConnections())
    {
        printf("Proxy closed with error!\n");
        return nullptr;
    }

    pthread_cleanup_pop(1);
    return nullptr;
}

void *worker_routine(void *args)
{
    auto *parent = (ProxyCore*)args;
    std::pair<int, int> current_task;
    ConnectionHandler *handler;

    while(true)
    {
        if((current_task = parent->getTask()).first == -1)
            return nullptr;

        handler = parent->getHandlerBySocket(current_task.first);
        bool res = !handler->execute(current_task.second);

        if(res)
            parent->removeHandler(current_task.first);
        else
            parent->madeSocketFreeForPoll(current_task.first);
    }
}