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

    sock = socket(AF_INET, SOCK_STREAM, 0);

    if(sock == -1)
    {
        perror("[PROXY-ERROR] Can't create server socket");
        return;
    }

    if (bind(sock, (sockaddr *) &addr, sizeof(addr)) == -1)
    {
        perror("[PROXY-ERROR] Can't bind socket on port");
        close(sock);
        return;
    }

    if(!initSocket(sock) || !initPollSet())
        return;

    Client::initHTTPParser();
    Server::initHTTPParser();
    sem_init(&lock, 0, 1);

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

    task_list.lock();
    task_list.notifyAll();
    task_list.unlock();

    for (auto thread : thread_pool)
        pthread_cancel(thread);

    thread_pool.clear();
    poll_set.clear();
    socketHandlers.clear();
    task_list.clear();

    sem_destroy(&lock);

    for (auto & i : poll_set)
    {
        closeSocket(i.fd);

        if (socketHandlers[i.fd])
            delete(socketHandlers[i.fd]);
    }
    for (auto & i : busy_set)
    {
        closeSocket(i.first);

        if (socketHandlers[i.first])
            delete(socketHandlers[i.first]);
    }
}

bool ProxyCore::listenConnections()
{
    int count;
    int revent;
    int new_client;

    printf("[PROXY--CORE] Proxy started on port: %d\n", port);

    while (true)
    {
        lockHandlers();
        //lock.lock();
        count = poll(poll_set.data(), (nfds_t) poll_set.size(), poll_set.size() == 1 && busy_set.empty() ? -1 : 1);

        if (count == -1)
        {
            if (errno == EINTR)
                continue;

            fprintf(stderr, "[PROXY-ERROR] Poll error: %s\n", strerror(errno));
            //lock.unlock();
            unlockHandlers();
            break;
        }else if (count == 0)
        {
            //lock.unlock();
            unlockHandlers();
            continue;
        }
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

                    if(!i)
                    {
                        if (revent & (POLLIN | POLLPRI))
                        {
                            new_client = accept(sock, nullptr, nullptr);
                            if (new_client == -1)
                            {
                                perror("[PROXY-ERROR] Can't accept new client");
                                clearData();
                                //lock.unlock();
                                unlockHandlers();
                                return false;
                            }

                            if (fcntl(new_client, F_SETFL, O_NONBLOCK) == -1)
                            {
                                perror("[PROXY-ERROR] Can't set nonblock to client socket");

                                closeSocket(new_client);
                                clearData();
                                //lock.unlock();
                                unlockHandlers();
                                return false;
                            }

                            Client *client;
                            try
                            {
                                client = new Client(new_client, this);
                            } catch (std::bad_alloc &e)
                            {
                                perror("[PROXY-ERROR] Can't allocate new client");

                                closeSocket(new_client);
                                clearData();
                                //lock.unlock();
                                unlockHandlers();
                                return false;
                            }

                            if(!addSocketToPollWithoutBlocking(new_client, POLLIN | POLLPRI, client))
                            {
                                fprintf(stderr, "Can't add new socket to poll! Closing...");
                                closeSocket(new_client);
                                clearData();
                                //lock.unlock();
                                unlockHandlers();
                                return false;
                            }

                            printf("[PROXY--INFO] Connected new user with socket %d\n", new_client);
                        } else
                        {
                            fprintf(stderr, "[PROXY-ERROR] Can't accept new client! Closing proxy...\n");
                            clearData();
                            //lock.unlock();
                            unlockHandlers();
                            return false;
                        }
                    }else
                    {
                        /*
                          FIXME надо делать проверку, какие таски уже выполняются,
                          FIXME чтоб два потока случайно не занялись одной работой(один тупо сожрёт данные другого и другой получить по ебалу EAGAINом)
                        */
                        /*
                         FIXME сделай систему коммитов в список дескрипторов для poll и блокировку ставь только на время внесения изменений
                         FIXME Это существенно снизит время блокировки основного монитора потока... надеюсь....
                         */
                        task_list.lock();
                        auto p = std::make_pair(poll_set[i].fd, revent);
                        if(!task_list.count(p))
                        {
                            task_list.insert(p);
                            busy_set[poll_set[i].fd] = poll_set[i];
                        }

                        task_list.notify();
                        task_list.unlock();
                    }
                }
            }
        }
        for (auto &i : busy_set)
            poll_set.erase(std::remove(poll_set.begin(), poll_set.end(), i.second), poll_set.end());

        //lock.unlock();
        unlockHandlers();
        sleep(0);
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

bool ProxyCore::addSocketToPoll(int socket, short events, ConnectionHandler *executor)
{
    //lock.lock();
    lockHandlers();
    bool success = addSocketToPollWithoutBlocking(socket, events, executor);
    //lock.unlock();
    unlockHandlers();

    return success;
}

bool ProxyCore::initSocket(int sock_fd)
{
    sock = sock_fd;

    if (fcntl(sock, F_SETFL, O_NONBLOCK) == -1)
    {
        perror("[PROXY-ERROR] Can't make socket nonblocking!");
        clearData();
        return false;
    }
    if (listen(sock, POLL_SIZE_SEGMENT) == -1)
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
    //lock.lock();
    lockHandlers();
    ssize_t pos = getSocketIndex(socket);
    if (pos != -1)
        poll_set[pos].events |= POLLOUT;
    //lock.unlock();
    unlockHandlers();
}

void ProxyCore::setSocketUnavailableToSend(int socket)
{
    //lock.lock();
    lockHandlers();
    ssize_t pos = getSocketIndex(socket);
    if (pos != -1)
        poll_set[pos].events &= ~POLLOUT;
    //lock.unlock();
    unlockHandlers();
}

bool ProxyCore::initPollSet()
{
    pollfd fd{};
    fd.fd = sock;
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

void ProxyCore::closeSocket(int _sock) const
{
    shutdown(_sock, _sock == sock ? SHUT_RDWR : SHUT_WR);
    close(_sock);
}

void ProxyCore::removeHandler(int socket)
{
    //lock.lock();
    lockHandlers();

    if(!socketHandlers[socket])
        return;

    printf("[PROXY--CORE] %s socket %i closed\n", instanceOf<Client>(socketHandlers[socket]) ? "Client" : "Server", socket);

    delete(socketHandlers[socket]);

    socketHandlers.erase(socket);
    for(auto iter = poll_set.begin(); iter != poll_set.end(); ++iter)
        if((*iter).fd == socket)
        {
            poll_set.erase(iter);
            break;
        }

    closeSocket(socket);
    //lock.unlock();
    unlockHandlers();
}

std::pair<int, int> ProxyCore::getTask()
{
    task_list.lock();

    while(task_list.empty())
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
    //lock.lock();
    if(sem_wait(&lock))
        printf("error sem wait!\n");
}

void ProxyCore::unlockHandlers()
{
    //lock.unlock();
    if(sem_post(&lock))
        printf("error sem post!\n");
}

void ProxyCore::madeSocketFreeForPoll(int _sock)
{
    //lock.lock();
    lockHandlers();
    poll_set.push_back(busy_set[_sock]);
    busy_set.erase(_sock);
    //lock.unlock();
    unlockHandlers();
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
    pthread_setcanceltype(PTHREAD_CANCEL_DEFERRED, nullptr);
    auto *parent = (ProxyCore*)args;
    std::pair<int, int> current_task;
    ConnectionHandler *handler;

    while(true)
    {
        pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, nullptr);
        current_task = parent->getTask();
        pthread_setcancelstate(PTHREAD_CANCEL_DISABLE, nullptr);

        if(current_task.first == -1)
        {
            pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, nullptr);
            return nullptr;
        }

        parent->lockHandlers();
        handler = parent->getHandlerBySocket(current_task.first);
        parent->unlockHandlers();

        bool res = !handler->execute(current_task.second);

        if(res)
            parent->removeHandler(current_task.first);
        else
            parent->madeSocketFreeForPoll(current_task.first);
    }
}
