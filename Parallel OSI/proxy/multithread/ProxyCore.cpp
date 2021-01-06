#include "ProxyCore.h"
#include "Client.h"

void *routine(void* args);
void *worker_routine(void *args);
void *cache_timer_routine(void *args);

pthread_t initProxyCore(std::tuple<int, int, int*> &args)
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
        closeSocket(proxy_socket, true);
        return;
    }

    if(pipe(poll_pipe))
    {
        perror("Can't init pipe for poll");
        closeSocket(proxy_socket, true);
        return;
    }

    if(!initSocket(proxy_socket) || !initPollSet())
    {
        close(poll_pipe[0]);
        close(poll_pipe[1]);
        closeSocket(proxy_socket, true);
        return;
    }

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

            close(poll_pipe[0]);
            close(poll_pipe[1]);
            closeSocket(proxy_socket, true);
            return;
        }
        thread_pool.push_back(thread);
    }

    if(pthread_create(&cache_timer_thread, nullptr, cache_timer_routine, (void*)this))
    {
        printf("Can't create cache timer thread for proxy! Shutting down...\n");

        for (auto j : thread_pool)
            pthread_cancel(j);

        close(poll_pipe[0]);
        close(poll_pipe[1]);
        closeSocket(proxy_socket, true);
        return;
    }

    printf("\n[PROXY--CORE] Proxy created!\n");

    created = true;
}

ProxyCore::~ProxyCore()
{
    if(!isCreated())
        return;

#ifdef DEBUG_ENABLED
    printf("Closing childs...\n");
#endif

    closed = true;

    task_list.lock();
    task_list.notifyAll();
    task_list.unlock();

    for(auto &thr : thread_pool)
        pthread_join(thr, nullptr);

    pthread_cancel(cache_timer_thread);
    pthread_join(cache_timer_thread, nullptr);

#ifdef DEBUG_ENABLED
    printf("End closing childs...\n");
    printf("Closing sockets...\n");
#endif

    close(poll_pipe[0]);
    close(poll_pipe[1]);
    poll_set.erase(poll_set.begin());
    for (auto & i : poll_set)
    {
        closeSocket(i.fd, i.fd == proxy_socket);

        if (socket_handlers.count(i.fd))
            delete(socket_handlers[i.fd]);
    }

#ifdef DEBUG_ENABLED
    printf("End closing sockets...\n");
    printf("Clear data and closing monitors...\n");
#endif

    thread_pool.clear();
    socket_handlers.clear();
    socket_positions.clear();
    new_server_set.clear();
    sock_rdwr.clear();
    task_list.clear();
    task_order.clear();
    trash_set.clear();
    free_set.clear();
    poll_set.clear();
    busy_set.clear();

    unlockMonitor(add_lock);
    unlockMonitor(free_lock);
    unlockMonitor(remove_lock);
    unlockMonitor(rdwr_lock);

    printf("\n[PROXY--CORE] Proxy closed!\n");

    created = false;
}

bool ProxyCore::listenConnections()
{
    int count;
    int revent;

    printf("[PROXY--CORE] Proxy started on port: %d\n", port);

    while (true)
    {
        if(!addServerConnections())
            return false;
        handleBusyConnections();
        markPollSockets();
        deleteDeadConnections();

        count = poll(poll_set.data(), (nfds_t) poll_set.size(), -1);

        if (count == -1)
        {
            if (errno == EINTR)
                continue;

            fprintf(stderr, "[PROXY-ERROR] Poll error: %s\n", strerror(errno));
            break;
        }else if (count == 0 && !have_marked_connections)
            continue;
        else
            for (size_t i = 0; i < poll_set.size(); ++i)
                if (!(revent = poll_set[i].revents))
                    continue;
                else
                {
                    if(i <= 1)
                        poll_set[i].revents = 0;

                    if(i == 0 && (revent & (POLLIN | POLLPRI)))//listen poll notifier
                    {
                        SIG_SAFE_IO_BLOCK2(read(poll_set[0].fd, &pipe_data, sizeof(char)))
                        if(pipe_data == CLOSE_SIGNAL)
                            return true;//safe section of code, we can go out of here
                        else
                            continue;//it's was wakeup from handlers to handle new tasks
                    }else if(i == 1 && (!(revent & (POLLIN | POLLPRI)) || !addClientConnection()))//it's proxy socket
                    {
                        fprintf(stderr, "[PROXY-ERROR] Can't accept new client! Closing proxy...\n");
                        return false;
                    }else if(i > 1)
                    {
                        task_list.lock();
                        if(!busy_set[poll_set[i].fd] && !task_list.count(poll_set[i].fd))
                        {
                            poll_set[i].revents = 0;
                            addNewTask(poll_set[i].fd, revent);
                        }
                        task_list.unlock();
                    }
                }
        have_marked_connections = false;
    }

    return false;//proxy will be here because of poll was broken
}

template<typename Base, typename T>
inline bool instanceOf(const T *ptr)
{
    return dynamic_cast<const Base*>(ptr) != nullptr;
}

bool ProxyCore::addSocketToPoll(int socket, short events, ConnectionHandler *executor)
{
    pollfd fd{};
    fd.fd = socket;
    fd.events = events;
    fd.revents = 0;

    bool success = true;
    try
    {
        socket_handlers[socket] = executor;
        socket_positions[socket] = poll_set.size();
        busy_set[socket] = false;
        poll_set.push_back(fd);
    } catch (std::bad_alloc &e)
    {
        perror("[PROXY-ERROR] Can't allocate new pollfd or handler");
        success = false;
    }

    return success;
}

bool ProxyCore::addSocketToPollQueue(int socket, ConnectionHandler *executor)
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
        return false;
    }

    return true;
}

ssize_t ProxyCore::getSocketIndex(int _sock)
{
    return socket_positions.count(_sock) ? socket_positions[_sock] : -1;
}

void ProxyCore::setSocketAvailableToSend(int socket)
{
    rdwr_lock.lock();
    sock_rdwr[socket] = true;
    rdwr_lock.unlock();
}

void ProxyCore::setSocketUnavailableToSend(int socket)
{
    rdwr_lock.lock();
    sock_rdwr[socket] = false;
    rdwr_lock.unlock();
}

bool ProxyCore::initPollSet()
{
    pollfd fd{};
    fd.fd = poll_pipe[0];
    fd.events = POLLIN | POLLPRI;
    fd.revents = 0;

    try
    {
        poll_set.emplace_back(fd);
        fd.fd = proxy_socket;
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
    return !socket_handlers.count(socket) ? nullptr : socket_handlers[socket];
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
    if(closed)
        return std::make_pair(-1,-1);

    task_list.lock();

    while(task_order.empty() && !closed)
        task_list.wait();

    if(closed)
    {
        task_list.unlock();
        return std::make_pair(-1,-1);
    }

    int next_sock = task_order.front();
    auto elem = task_list[next_sock];

    task_order.pop_front();
    task_list.erase(next_sock);

    busy_set[next_sock] = true;
    task_list.unlock();

    return std::make_pair(next_sock, elem);
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

void ProxyCore::removeHandlerImpl(int sock)
{
    if(!socket_handlers.count(sock))
        return;

    printf("[PROXY--CORE] %s socket %i closed\n", instanceOf<Client>(socket_handlers[sock]) ? "Client" : "Server", sock);

    delete(socket_handlers[sock]);

    socket_handlers.erase(sock);
    poll_set.erase(poll_set.begin() + socket_positions[sock]);

    task_list.erase(sock);
    task_order.remove_if([=](int elem){ return elem == sock; });
    busy_set[sock] = false;

    socket_positions.clear();
    for (int i = 0; i < poll_set.size(); ++i)
        socket_positions[poll_set[i].fd] = i;

    closeSocket(sock, false);
}

void ProxyCore::unlockMonitor(Monitor monitor)
{
    if(monitor.isLocked())
        monitor.unlock();
}

void ProxyCore::markPollSockets()
{
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
    have_marked_connections = !sock_rdwr.empty();
    sock_rdwr.clear();
    rdwr_lock.unlock();
}

bool ProxyCore::addServerConnections()
{
    add_lock.lock();
    for (auto &i : new_server_set)
        if (!addSocketToPoll(i.first, POLLIN | POLLPRI | POLLOUT, i.second))
        {
            fprintf(stderr, "Can't add new server socket to poll from add set! Closing...");
            closeSocket(i.first, false);
            return false;
        }
    new_server_set.clear();
    add_lock.unlock();

    return true;
}

void ProxyCore::handleBusyConnections()
{
    task_list.lock();
    free_lock.lock();
    for (auto &i : free_set)
    {
        busy_set[i] = false;
        int pos = getSocketIndex(i);
        if(pos != -1)
            poll_set[pos].events |= POLLIN | POLLPRI;
    }
    free_set.clear();
    free_lock.unlock();

    for (auto &s : busy_set)
    {
        int pos = getSocketIndex(s.first);
        if(pos != -1 && busy_set[s.first])
            poll_set[pos].events &= ~(POLLIN | POLLPRI);
    }
    task_list.unlock();
}

void ProxyCore::deleteDeadConnections()
{
    lockHandlers();
    task_list.lock();
    for(auto it = trash_set.begin();it != trash_set.end();)
        if(!busy_set[*it])
        {
            removeHandlerImpl(*it);
            it = trash_set.erase(it);
        }else
            it++;
    task_list.unlock();
    unlockHandlers();
}

bool ProxyCore::addClientConnection()
{
    int new_client;

    SIG_SAFE_IO_BLOCK(accept(proxy_socket, nullptr, nullptr), new_client)
    if (new_client == -1)
    {
        perror("[PROXY-ERROR] Can't accept new socket");
        return false;
    }

    Client *client;
    try
    {
        client = new Client(new_client, this);
    } catch (std::bad_alloc &e)
    {
        perror("[PROXY-ERROR] Can't allocate memory for new connection handler");
        closeSocket(new_client, false);
        return false;
    }

    if (!addSocketToPoll(new_client, POLLIN | POLLPRI, client))
    {
        fprintf(stderr, "[PROXY-ERROR] Can't add new socket to poll!\n");
        closeSocket(new_client, false);
        return false;
    }

    printf("[PROXY--INFO] Connected new user with socket %d\n", new_client);
    return true;
}

void ProxyCore::addNewTask(int sock, int revent)
{
    lockHandlers();
    bool not_deleted = (trash_set.empty() || std::find(trash_set.begin(), trash_set.end(), sock) == trash_set.end());
    if (not_deleted)
    {
        task_list[sock] = revent;
        task_order.push_back(sock);
        task_list.notify();
    }
    unlockHandlers();
}

void ProxyCore::noticePoll()
{
    SIG_SAFE_IO_BLOCK2(write(poll_pipe[1], &pipe_data, sizeof(char)))
}

int ProxyCore::getProxyNotifier()
{
    return poll_pipe[1];
}

bool ProxyCore::isClosed() const
{
    return closed;
}

void *routine(void *args)
{
    ProxyCore *proxy;

    auto *p = (std::tuple<int, int, int*>*)args;

    try
    {
        proxy = new ProxyCore(std::get<0>(*p), std::get<1>(*p));
    } catch (std::bad_alloc &e)
    {
        perror("Can't init proxy");
        return nullptr;
    }

    if(!proxy->isCreated())
    {
        perror("Can't init proxy");
        delete(proxy);
        return nullptr;
    }

    *(std::get<2>(*p)) = proxy->getProxyNotifier();

    if (!proxy->listenConnections())
        printf("Proxy closed with error!\n");

    delete(proxy);
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

        if(!(handler = parent->getHandlerBySocket(current_task.first)))
            continue;

        bool res = !handler->execute(current_task.second);

        parent->madeSocketFreeForPoll(current_task.first);
        if(res)
            parent->removeHandler(current_task.first);

        parent->noticePoll();
    }
}

void *cache_timer_routine(void *args)
{
    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, nullptr);
    pthread_setcanceltype(PTHREAD_CANCEL_DEFERRED, nullptr);
    auto *parent = (ProxyCore*)args;

    while(!parent->isClosed())
    {
        parent->lockHandlers();
        Cache::getCache().updateTimers();
        parent->unlockHandlers();

        sleep(1);
    }

    return nullptr;
}