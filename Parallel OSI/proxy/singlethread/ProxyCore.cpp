#include "ProxyCore.h"
#include "Client.h"

ProxyCore::ProxyCore(int socket_fd)
{
    if(!initSocket(socket_fd) || !initPollSet() || !initConnectionHandlers())
        return;

    Client::initHTTPParser();
    Server::initHTTPParser();

    printf("\nProxy created!\n");

    created = true;
    can_work = true;
}

ProxyCore::~ProxyCore() {
    clearData();
    printf("\nProxy closed!\n");

    created = false;
    can_work = false;
}

void ProxyCore::clearData()
{
    for (size_t i = 0; i < poll_cur_size; ++i)
    {
        close(poll_set[i].fd);
        if (c_handlers[i])
            delete c_handlers[i];
    }
    free(poll_set);
    free(c_handlers);
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

            fprintf(stderr, "Poll error: %s\n", strerror(errno));
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
                            perror("Can't accept new client");
                            clearData();
                            return false;
                        }
                        if (fcntl(new_client, F_SETFL, O_NONBLOCK) == -1)
                        {
                            perror("Couldn't set nonblock to client socket");
                            clearData();
                            return false;
                        }

                        Client *client;
                        try
                        {
                            client = new Client(new_client, this);
                        } catch (std::bad_alloc &e)
                        {
                            perror("Can't allocate new client");
                            clearData();
                            return false;
                        }

                        addSocketToPoll(new_client, POLLIN | POLLPRI, client);

                        printf("Connected new user with socket %d\n", new_client);
                    } else
                    {
                        fprintf(stderr, "Can't accept new client! Closing proxy...\n");
                        clearData();
                        return false;
                    }
                }else if (!c_handlers[i]->execute(revent)) /* listen client side */
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
    {
        ssize_t pos = getSocketIndex(it);
        if (pos == -1)
            return;

        removeSocketByIndex(pos);
    }

    trashbox->clear();
}

void ProxyCore::removeSocketByIndex(size_t pos)
{
    printf("Socket %d closed\n", poll_set[pos].fd);

    --poll_cur_size;
    close(poll_set[pos].fd);
    delete c_handlers[pos];
    memcpy(&poll_set[pos], &poll_set[poll_cur_size], sizeof(pollfd));
    memcpy(&c_handlers[pos], &c_handlers[poll_cur_size], sizeof(ConnectionHandler *));
}

void ProxyCore::removeSocket(std::set<int> *trashbox, int socket)
{
    ssize_t pos = getSocketIndex(socket);
    if (pos == -1)
        return;

    trashbox->insert(socket);
}

bool ProxyCore::addSocketToPoll(int socket, short events, ConnectionHandler *executor) {
    if (poll_cur_size + 1 >= poll_size)
    {
        poll_size += POLL_SIZE_SEGMENT;
        auto *poll_tmp = (pollfd *) realloc(poll_set, poll_size * sizeof(pollfd));

        if (!poll_tmp)
        {
            perror("Can't allocate new pollfd");
            return false;
        }
        poll_set = poll_tmp;

        auto **handlers_tmp = (ConnectionHandler **) realloc(c_handlers, poll_size * sizeof(ConnectionHandler *));
        if (!handlers_tmp)
        {
            perror("Can't allocate new handlers");
            return false;
        }
        c_handlers = handlers_tmp;
    }

    poll_set[poll_cur_size].fd = socket;
    poll_set[poll_cur_size].events = events;
    poll_set[poll_cur_size].revents = 0;
    c_handlers[poll_cur_size] = executor;
    ++poll_cur_size;
    return true;
}

bool ProxyCore::initSocket(int sock_fd)
{
    sock = sock_fd;

    if (fcntl(sock, F_SETFL, O_NONBLOCK) == -1)
    {
        perror("Can't make socket nonblocking!");
        clearData();
        return false;
    }
    if (listen(sock, POLL_SIZE_SEGMENT) == -1) {
        perror("Couldn't set listen to server socket");
        clearData();
        return false;
    }

    return true;
}

ssize_t ProxyCore::getSocketIndex(int socket)
{
    for (size_t i = 0; i < poll_cur_size; ++i)
        if (poll_set[i].fd == socket)
            return i;

    return -1;
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
        perror("Cannot allocate memory for poll set!");
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
    c_handlers = (ConnectionHandler **) malloc(poll_size * sizeof(ConnectionHandler*));
    if(!c_handlers)
    {
        perror("Cannot allocate memory for handler set!");
        return false;
    }
    return true;
}
