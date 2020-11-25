#include "ProxyCore.h"
#include "Client.h"

ProxyCore::ProxyCore(int socket_fd)
{
    if(!initSocket(socket_fd) || !initPollSet() || !initConnectionHandlers())
        return;

    Client::initHTTPParser();
    Server::initHTTPParser();

    printf("\n[PROXY--CORE] Proxy created!\n");

    created = true;
    can_work = true;
}

ProxyCore::~ProxyCore() {
    clearData();
    printf("\n[PROXY--CORE] Proxy closed!\n");

    created = false;
    can_work = false;
}

void ProxyCore::clearData()
{
    for (size_t i = 0; i < poll_set.size(); ++i)
    {
        shutdown(poll_set[i].fd, SHUT_WR);
        close(poll_set[i].fd);

        if (connection_handlers[i])
            delete connection_handlers[i];
    }

    poll_set.clear();
    socket_pos.clear();
    connection_handlers.clear();

    Cache::getCache().clearCache();
}

bool ProxyCore::listenConnections()
{
    int count;
    int revent;
    int new_client;

    std::set<int> trashbox;
    bool can_remove = false;
    while (can_work)
    {
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

                poll_set[i].revents = 0;

                /* listen new connections */
                if(!i)
                {
                    if (revent & (POLLIN | POLLPRI))
                    {
                        new_client = accept(sock, NULL, NULL);
                        if (new_client == -1)
                        {
                            perror("[PROXY-ERROR] Can't accept new client");
                            clearData();
                            return false;
                        }
                        
                        if (fcntl(new_client, F_SETFL, O_NONBLOCK) == -1)
                        {
                            perror("[PROXY-ERROR] Can't set nonblock to client socket");
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
                            clearData();
                            return false;
                        }

                        addSocketToPoll(new_client, POLLIN | POLLPRI, client);

                        printf("[PROXY--INFO] Connected new user with socket %d\n", new_client);
                    } else
                    {
                        fprintf(stderr, "[PROXY-ERROR] Can't accept new client! Closing proxy...\n");
                        clearData();
                        return false;
                    }
                }else if (!connection_handlers[i]->execute(revent)) /* listen client side */
                {
                    removeSocket(&trashbox, poll_set[i].fd);
                    can_remove = true;
                }

                if(can_remove)
                {
                    can_remove = false;
                    removeClosedSockets(&trashbox);
                }
            }
        }
    }
    return false;
}

void ProxyCore::removeClosedSockets(std::set<int> *trashbox)
{
    for (int it : *trashbox)
        removeSocketByIndex(it);

    trashbox->clear();
}

template<typename Base, typename T>
inline bool instanceOf(const T *ptr)
{
    return dynamic_cast<const Base*>(ptr) != nullptr;
}

void ProxyCore::removeSocketByIndex(size_t pos)
{
    printf("[PROXY--CORE] %s socket %d closed\n", instanceOf<Client>(connection_handlers[pos]) ? "Client" : "Server", poll_set[pos].fd);

    shutdown(poll_set[pos].fd, SHUT_WR);
    close(poll_set[pos].fd);

    socket_pos.erase(pos);
    socket_pos[poll_set[poll_set.size() - 1].fd] = pos;

    delete(connection_handlers[pos]);

    poll_set[pos] = poll_set[poll_set.size() - 1];
    connection_handlers[pos] = connection_handlers[connection_handlers.size() - 1];

    connection_handlers.pop_back();
    poll_set.pop_back();
}

void ProxyCore::removeSocket(std::set<int> *trashbox, int socket)
{
    ssize_t pos = getSocketIndex(socket);
    if (pos == -1)
        return;
    else
        trashbox->insert(pos);
}

bool ProxyCore::addSocketToPoll(int socket, short events, ConnectionHandler *executor)
{
    pollfd fd{};
    fd.fd = socket;
    fd.events = events;
    fd.revents = 0;

    try
    {
        poll_set.emplace_back(fd);
        connection_handlers.emplace_back(executor);
    } catch (std::bad_alloc &e)
    {
        perror("[PROXY-ERROR] Can't allocate new pollfd or handler");
        return false;
    }

    socket_pos[socket] = connection_handlers.size() - 1;

    return true;
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

ssize_t ProxyCore::getSocketIndex(int socket)
{
    if(!socket_pos.count(socket))
        return -1;
    else
        return socket_pos[socket];
}

bool ProxyCore::setSocketAvailableToSend(int socket)
{
    ssize_t pos = getSocketIndex(socket);
    if (pos == -1)
        return false;

    poll_set[pos].events |= POLLOUT;
    return true;
}

bool ProxyCore::setSocketUnavailableToSend(int socket)
{
    ssize_t pos = getSocketIndex(socket);
    if (pos == -1)
        return false;

    poll_set[pos].events &= ~POLLOUT;
    return true;
}

bool ProxyCore::initConnectionHandlers()
{
    try
    {
        connection_handlers.emplace_back(nullptr);
    } catch (std::bad_alloc &e)
    {
        perror("[PROXY-ERROR] Can't allocate memory for handler!");
        return false;
    }

    return true;
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

bool ProxyCore::isCreated()
{
    return created;
}

ConnectionHandler *ProxyCore::getHandlerBySocket(int socket)
{
    return !socket_pos.count(socket) ? nullptr : connection_handlers[socket_pos[socket]];
}
