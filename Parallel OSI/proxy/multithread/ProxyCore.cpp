#include "ProxyCore.h"
#include "Client.h"

void *routine(void* args);

[[noreturn]]
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

    this->port = port;

    pthread_mutexattr_t attr;
    pthread_mutexattr_init(&attr);
    pthread_mutexattr_settype(&attr, PTHREAD_MUTEX_ERRORCHECK);
    pthread_mutex_init(&core_mutex, &attr);
    pthread_mutex_init(&task_mutex, &attr);
    pthread_cond_init(&task_cond, nullptr);

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
    pthread_mutex_destroy(&core_mutex);
    pthread_mutex_destroy(&task_mutex);
    pthread_cond_destroy(&task_cond);
    printf("\n[PROXY--CORE] Proxy closed!\n");

    created = false;
}

void ProxyCore::clearData()
{
    lock_guard lock(&core_mutex);

    for (auto & i : poll_set)
    {
        closeSocket(i.fd);

        if (socketHandlers[i.fd])
            delete(socketHandlers[i.fd]);
    }

    for (auto thread : thread_pool)
    {
        pthread_cancel(thread);
        //pthread_join(thread, nullptr);
    }

    thread_pool.clear();
    poll_set.clear();
    socketHandlers.clear();
    task_list.clear();
    std::queue<std::pair<int, int>> empty;
    std::swap(task_queue, empty);
}

bool ProxyCore::listenConnections()
{
    int count;
    int revent;
    int new_client;

    printf("[PROXY--CORE] Proxy started on port: %d\n", port);

    while (true)
    {
        if(!task_queue.empty())
            continue;

        pthread_mutex_lock(&core_mutex);
        count = poll(poll_set.data(), (nfds_t) poll_set.size(), -1);

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

                    if(!i)
                    {
                        if (revent & (POLLIN | POLLPRI))
                        {
                            new_client = accept(sock, nullptr, nullptr);
                            if (new_client == -1)
                            {
                                perror("[PROXY-ERROR] Can't accept new client");
                                clearData();
                                return false;
                            }

                            if (fcntl(new_client, F_SETFL, O_NONBLOCK) == -1)
                            {
                                perror("[PROXY-ERROR] Can't set nonblock to client socket");

                                closeSocket(new_client);
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

                                closeSocket(new_client);
                                clearData();
                                return false;
                            }

                            addSocketToPoll(true, new_client, POLLIN | POLLPRI, client);

                            printf("[PROXY--INFO] Connected new user with socket %d\n", new_client);
                        } else
                        {
                            fprintf(stderr, "[PROXY-ERROR] Can't accept new client! Closing proxy...\n");
                            clearData();
                            return false;
                        }
                    }else
                    {
                        lock_guard lock(&task_mutex);
                        if(!task_list.count(poll_set[i].fd))
                        {
                            task_list.insert(poll_set[i].fd);
                            task_queue.push(std::make_pair(poll_set[i].fd, revent));
                            pthread_cond_signal(&task_cond);
                        }
                    }
                }
            }
        }
        pthread_mutex_unlock(&core_mutex);
    }

    return false;
}

template<typename Base, typename T>
inline bool instanceOf(const T *ptr)
{
    return dynamic_cast<const Base*>(ptr) != nullptr;
}

void ProxyCore::removeSocket(size_t _sock)
{
    printf("[PROXY--CORE] %s socket %zd closed\n", instanceOf<Client>(socketHandlers[_sock]) ? "Client" : "Server", _sock);

    delete(socketHandlers[_sock]);

    socketHandlers.erase(_sock);
    for(auto iter = poll_set.begin(); iter != poll_set.end(); ++iter)
        if((*iter).fd == _sock)
        {
            poll_set.erase(iter);
            break;
        }

    closeSocket(_sock);
}

bool ProxyCore::addSocketToPoll(bool from_proxy_thread, int socket, short events, ConnectionHandler *executor)
{
    pollfd fd{};
    fd.fd = socket;
    fd.events = events;
    fd.revents = 0;

    bool success = true;
    if(!from_proxy_thread)
        lock_guard lock(&core_mutex);
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
    lock_guard lock(&core_mutex);

    ssize_t pos = getSocketIndex(socket);
    if (pos != -1)
        poll_set[pos].events |= POLLOUT;
}

void ProxyCore::setSocketUnavailableToSend(int socket)
{
    lock_guard lock(&core_mutex);

    ssize_t pos = getSocketIndex(socket);
    if (pos != -1)
        poll_set[pos].events &= ~POLLOUT;
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
    lock_guard lock(&core_mutex);
    return !socketHandlers.count(socket) ? nullptr : socketHandlers[socket];
}

void ProxyCore::closeSocket(int _sock) const
{
    shutdown(_sock, _sock == sock ? SHUT_RDWR : SHUT_WR);
    close(_sock);
}

void ProxyCore::clearHandler(int socket)
{
    lock_guard lock(&core_mutex);
    removeSocket(socket);
}

std::pair<int, int> ProxyCore::getTask()
{
    pthread_mutex_lock(&task_mutex);

    while(task_queue.empty())
        pthread_cond_wait(&task_cond, &task_mutex);

    auto elem = task_queue.front();
    task_queue.pop();
    task_list.erase(elem.first);

    pthread_mutex_unlock(&task_mutex);
    return elem;
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

    auto *pair = (std::pair<int, int>*)args;

    try
    {
        proxy = new ProxyCore(pair->first, pair->second);
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

[[noreturn]]
void *worker_routine(void *args)
{
    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, nullptr);
    pthread_setcanceltype(PTHREAD_CANCEL_DEFERRED, nullptr);

    auto *parent = (ProxyCore*)args;
    std::pair<int, int> current_task;
    ConnectionHandler *handler;

    while(true)
    {
        current_task = parent->getTask();
        handler = parent->getHandlerBySocket(current_task.first);

        if(!handler->execute(current_task.second))
            parent->clearHandler(current_task.first);
    }
}
