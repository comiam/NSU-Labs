#include <csignal>
#include "ProxyCore.h"
#include "Client.h"

void *routine(void* args);

void *worker_routine(void *args);
void *poll_routine(void *args);

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
        closeSocket(proxy_socket, true);
        return;
    }

    if(pipe(poll_call_fd))
    {
        perror("Can't create poll pipe callback");
        closeSocket(proxy_socket, true);
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

            closeSocket(proxy_socket, true);
            close(poll_call_fd[0]);
            close(poll_call_fd[1]);
            return;
        }
        thread_pool.push_back(thread);
    }

    if(pthread_create(&poll_thread, nullptr, poll_routine, (void*)this))
    {
        printf("Can't create poll thread for proxy! Shutting down...\n");

        for (auto j : thread_pool)
            pthread_cancel(j);
        closeSocket(proxy_socket, true);
        close(poll_call_fd[0]);
        close(poll_call_fd[1]);
        return;
    }

    printf("\n[PROXY--CORE] Proxy created!\n");

    created = true;
}

ProxyCore::~ProxyCore()
{
    if(created)
    {
        clearData();
        printf("\n[PROXY--CORE] Proxy closed!\n");

        created = false;
    }
    closing = true;
}

void ProxyCore::clearData()
{
    closing = true;

#ifdef DEBUG_ENABLED
    printf("Closing childs...\n");
#endif
    conveyor_task_list.lock();
    conveyor_task_list.notifyAll();
    conveyor_task_list.unlock();

    if(!poll_close_flag)
        pthread_cancel(poll_thread);

    pthread_join(poll_thread, nullptr);

    for(auto &thr : thread_pool)
        pthread_join(thr, nullptr);
#ifdef DEBUG_ENABLED
    printf("End closing childs...\n");
    printf("Closing sockets...\n");
#endif

    for (auto & i : poll_set)
    {
        if(i.fd == poll_call_fd[0])
            close(i.fd);
        else
        {
            closeSocket(i.fd, i.fd == proxy_socket);

            if (socketHandlers[i.fd])
                delete(socketHandlers[i.fd]);
        }
    }
    close(poll_call_fd[1]);
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
    poll_task_list.clear();
    handler_task_list.clear();
    conveyor_task_list.clear();
    trash_set.clear();
    free_set.clear();
    poll_set.clear();
    busy_set.clear();

    unlockMonitor(add_lock);
    unlockMonitor(free_lock);
    unlockMonitor(remove_lock);
    unlockMonitor(htask_lock);
    unlockMonitor(proxy_task_lock);
}

bool ProxyCore::listenConnections()
{
    printf("[PROXY--CORE] Proxy started on port: %d\n", port);

    while (true)
    {
        proxy_task_lock.lock();
        while(poll_task_list.empty() && handler_task_list.empty() && !poll_close_flag)
            proxy_task_lock.wait();

        freeBusySockets();
        mergeAndPushNewTaskToLists();

        if(poll_close_flag)
        {
            fprintf(stderr,"[PROXY-CORE] Poll thread died! Closing...\n");
            proxy_task_lock.unlock();
            return false;
        }
        proxy_task_lock.unlock();
    }
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

bool ProxyCore::addSocketToPoll(int socket, short events, ConnectionHandler *executor)
{
    pollfd fd{};
    fd.fd = socket;
    fd.events = events;
    fd.revents = 0;

    bool success = true;

    lockHandlers();
    try
    {
        socketHandlers[socket] = executor;
        busy_set[socket] = false;
        poll_set.push_back(fd);
    } catch (std::bad_alloc &e)
    {
        perror("[PROXY-ERROR] Can't allocate new pollfd or handler");
        success = false;
    }

    unlockHandlers();

    return success;
}

bool ProxyCore::addSocketToPollQueue(int socket, ConnectionHandler *executor)
{
    add_lock.lock();
    new_server_set.emplace_back(socket, executor);
    add_lock.unlock();

    printf("%zi\n", write(poll_call_fd[1], &poll_call_byte, sizeof(char)));

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
    htask_lock.lock();
    handler_task_list[socket] = POLLOUT;
    htask_lock.unlock();

    notifyConveyor();
}

void ProxyCore::setSocketUnavailableToSend(int socket)
{
    htask_lock.lock();
    handler_task_list.erase(socket);
    htask_lock.unlock();

    notifyConveyor();
}

bool ProxyCore::initPollSet()
{
    pollfd fd{};
    fd.fd = poll_call_fd[0];
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

    conveyor_task_list.lock();

    while(conveyor_task_list.empty() && !closing)
        conveyor_task_list.wait();

    if(closing)
    {
        conveyor_task_list.unlock();
        return std::make_pair(-1,-1);
    }

    auto elem = *conveyor_task_list.begin();
    conveyor_task_list.erase(elem);
    conveyor_task_list.unlock();

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

void ProxyCore::removeHandlerImpl(int socket)
{
    if(!socketHandlers.count(socket))
        return;

    printf("[PROXY--CORE] %s socket %i closed\n", instanceOf<Client>(socketHandlers[socket]) ? "Client" : "Server", socket);

    delete(socketHandlers[socket]);

    socketHandlers.erase(socket);
    poll_set.erase(
            std::remove_if(
                    poll_set.begin(),
                    poll_set.end(),
                    [socket](const pollfd &p) { return p.fd == socket; }
                ),
            poll_set.end());
    busy_set.erase(socket);

    closeSocket(socket, false);
}

void ProxyCore::unlockMonitor(Monitor monitor)
{
    if(monitor.isLocked())
        monitor.unlock();
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

void ProxyCore::deleteDeadConnections()
{
    lockHandlers();
    for(auto it = trash_set.begin();it != trash_set.end();)
        if(!busy_set.count(*it))
        {
            removeHandlerImpl(*it);
            it = trash_set.erase(it);
        }else
            it++;
    unlockHandlers();
}

bool ProxyCore::addClientConnection()
{
    int new_client = accept(proxy_socket, nullptr, nullptr);
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
        delete(client);
        return false;
    }

    printf("[PROXY--INFO] Connected new user with socket %d\n", new_client);
    return true;
}

void ProxyCore::addNewTask(int poll_position, int revent)
{
    conveyor_task_list.lock();
    pollfd val = poll_set[poll_position];
    auto p = std::make_pair(val.fd, revent);

    if (!conveyor_task_list.count(p) &&
        (std::find(trash_set.begin(), trash_set.end(), val.fd)
         ==
         trash_set.end()))
    {
        conveyor_task_list.insert(p);
        busy_set[val.fd] = true;
    }

    conveyor_task_list.notify();
    conveyor_task_list.unlock();
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

    if(!proxy->isCreated())
    {
        perror("Can't init proxy");
        cleanProxy(proxy);
        return nullptr;
    }

    pthread_cleanup_push(cleanProxy, proxy);
    if (!proxy->listenConnections())
    {
        printf("Proxy closed with error!\n");
        return nullptr;
    }
    pthread_cleanup_pop(1);

    return nullptr;
}

#define EXIT_POLL_THREAD                                                     \
                {                                                            \
                    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, nullptr);  \
                    *close_flag = -1;                                        \
                    break;                                                   \
                };

void *poll_routine(void *args)
{
    pthread_setcanceltype(PTHREAD_CANCEL_DEFERRED, nullptr);

    auto *parent = (ProxyCore*)args;
    auto *close_flag = &parent->poll_close_flag;

    *close_flag = 0;

    int count;
    int revent;

    bool first_state = true;
    bool task_detected = false;

    while (true)
    {
        if(first_state)
        {
            pthread_setcancelstate(PTHREAD_CANCEL_DISABLE, nullptr);
            first_state = false;
        }

        parent->deleteDeadConnections();
        if(!parent->addServerConnections())
            EXIT_POLL_THREAD

        pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, nullptr);
        count = poll(parent->poll_set.data(), (nfds_t) parent->poll_set.size(), -1);
        pthread_setcancelstate(PTHREAD_CANCEL_DISABLE, nullptr);

        if (count == -1)
        {
            if (errno == EINTR)
                continue;

            fprintf(stderr, "[PROXY-ERROR] Poll error: %s\n", strerror(errno));
            EXIT_POLL_THREAD
        } else if (count == 0)
            continue;
        else
        {
            for (size_t i = 0; i < parent->poll_set.size(); ++i)
                if (!(revent = parent->poll_set[i].revents))
                    continue;
                else
                {
                    parent->poll_set[i].revents = 0;

                    if(i == 0 && (revent & (POLLIN | POLLPRI)))//need for cancelling of poll
                    {
                        read(parent->poll_set[i].fd, &parent->poll_call_byte, sizeof(char));
                        continue;
                    }else if(i == 1 && (!(revent & (POLLIN | POLLPRI)) || !parent->addClientConnection()))//only for proxy socket
                    {
                        fprintf(stderr, "[PROXY-ERROR] Can't accept new client! Closing proxy...\n");
                        EXIT_POLL_THREAD
                    }else if(i != 1)
                    {
                        task_detected = true;
                        parent->pushNewPollEvent(parent->poll_set[i].fd, revent);
                    }
                }

            if(task_detected)
            {
                parent->notifyConveyor();
                task_detected = false;
            }
        }

    }
    return nullptr;
}

void ProxyCore::pushNewPollEvent(int pos, int revent)
{
    poll_task_list.lock();
    poll_task_list.push_back(std::make_pair(pos, revent));
    poll_task_list.unlock();
}

void ProxyCore::mergeAndPushNewTaskToLists()
{
    sigset_t set;

    sigemptyset(&set);
    sigaddset(&set, SIGPOLL);
    pthread_sigmask(SIG_UNBLOCK, &set, nullptr);

    poll_task_list.lock();
    htask_lock.lock();
    lockHandlers();

    int pos;
    for (auto &ptask : poll_task_list)
        if(!busy_set[ptask.first])
        {
            int new_revent = ptask.second;

            if(handler_task_list.count(ptask.first))
            {
                new_revent |= handler_task_list[ptask.first];
                handler_task_list.erase(ptask.first);
            }

            if(queued_tasks.count(ptask.first))
            {
                new_revent |= queued_tasks[ptask.first];
                queued_tasks.erase(ptask.first);
            }

            if((pos = getSocketIndex(ptask.first)) != -1)
                addNewTask(pos, new_revent);
        }else
            queued_tasks[ptask.first] |= (ptask.second | handler_task_list[ptask.first]);

    poll_task_list.clear();

    for (auto &htask : handler_task_list)
        if(!busy_set[htask.first])
        {
            int new_revent = htask.second;

            if(queued_tasks.count(htask.first))
            {
                new_revent |= queued_tasks[htask.first];
                queued_tasks.erase(htask.first);
            }

            if((pos = getSocketIndex(htask.first)) != -1)
                addNewTask(pos, new_revent);
        }else
            queued_tasks[htask.first] |= htask.second;

    handler_task_list.clear();

    for (auto qtask = queued_tasks.begin(); qtask != queued_tasks.end();)
        if(!busy_set[qtask->first] && (pos = getSocketIndex(qtask->first)) != -1)
        {
            addNewTask(pos, qtask->second);
            ++qtask;
        }else if(pos == -1)
            qtask = queued_tasks.erase(qtask);

    unlockHandlers();
    htask_lock.unlock();
    poll_task_list.unlock();
}

void ProxyCore::freeBusySockets()
{
    free_lock.lock();
    for (auto &i : free_set)
        busy_set[i] = false;

    free_set.clear();
    free_lock.unlock();
}

void ProxyCore::notifyConveyor()
{
    proxy_task_lock.lock();
    proxy_task_lock.notify();
    proxy_task_lock.unlock();
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
    }
}