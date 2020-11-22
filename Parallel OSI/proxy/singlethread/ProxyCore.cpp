#include "ProxyCore.h"
#include "Client.h"

ProxyCore::ProxyCore(int socket_fd)
{
    if(!initSocket(socket_fd) || !initPollSet() || !initConnectionHandlers())
        return;

    Client::initHTTPParser();
    Server::initHTTPParser();

    socket_pos = new std::map<int, size_t>();

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
    for (size_t i = 0; i < poll_cur_size; ++i)
    {
        shutdown(poll_set[i].fd, SHUT_RDWR);
        close(poll_set[i].fd);

        if (connection_handlers[i])
            delete connection_handlers[i];
    }

    free(poll_set);
    free(connection_handlers);
    delete(socket_pos);
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
        count = poll(poll_set, (nfds_t) poll_cur_size, -1);
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
            for (size_t i = 0; i < poll_cur_size; ++i)
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

    --poll_cur_size;
    shutdown(poll_set[pos].fd, SHUT_RDWR);
    close(poll_set[pos].fd);

    delete(connection_handlers[pos]);

    socket_pos->erase(pos);
    (*socket_pos)[poll_set[poll_cur_size].fd] = pos;

    memcpy(&poll_set[pos], &poll_set[poll_cur_size], sizeof(pollfd));
    memcpy(&connection_handlers[pos], &connection_handlers[poll_cur_size], sizeof(ConnectionHandler *));
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
    if (poll_cur_size + 1 >= poll_size)
    {
        poll_size += POLL_SIZE_SEGMENT;
        auto *poll_tmp = (pollfd *) realloc(poll_set, poll_size * sizeof(pollfd));

        if (!poll_tmp)
        {
            perror("[PROXY-ERROR] Can't allocate new pollfd");
            return false;
        }
        poll_set = poll_tmp;

        auto **handlers_tmp = (ConnectionHandler **) realloc(connection_handlers, poll_size * sizeof(ConnectionHandler *));
        if (!handlers_tmp)
        {
            perror("[PROXY-ERROR] Can't allocate new handlers");
            return false;
        }
        connection_handlers = handlers_tmp;
    }

    poll_set[poll_cur_size].fd = socket;
    poll_set[poll_cur_size].events = events;
    poll_set[poll_cur_size].revents = 0;
    connection_handlers[poll_cur_size] = executor;

    (*socket_pos)[socket] = poll_cur_size;

    ++poll_cur_size;
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
    if(!socket_pos->count(socket))
        return -1;
    else
        return (*socket_pos)[socket];
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

bool ProxyCore::initPollSet()
{
    poll_size = POLL_SIZE_SEGMENT;

    poll_set = (pollfd *) malloc(poll_size * sizeof(pollfd));
    if(!poll_set)
    {
        perror("[PROXY-ERROR] Can't allocate memory for poll set!");
        return false;
    }

    poll_cur_size = 1;
    poll_set[0].fd = sock;
    poll_set[0].events = POLLIN | POLLPRI;
    return true;
}

bool ProxyCore::isCreated()
{
    return created;
}

bool ProxyCore::initConnectionHandlers()
{
    connection_handlers = (ConnectionHandler **) calloc(poll_size, sizeof(ConnectionHandler*));
    if(!connection_handlers)
    {
        perror("[PROXY-ERROR] Can't allocate memory for handler set!");
        return false;
    }
    return true;
}

ConnectionHandler *ProxyCore::getHandlerBySocket(int socket)
{
    return !socket_pos->count(socket) ? nullptr : connection_handlers[(*socket_pos)[socket]];
}
