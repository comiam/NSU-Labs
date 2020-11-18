#ifndef SINGLETHREAD_PROXYCORE_H
#define SINGLETHREAD_PROXYCORE_H

#include <cstdlib>
#include <poll.h>
#include <fcntl.h>
#include <cstdio>
#include <sys/socket.h>
#include <unistd.h>
#include <cerrno>
#include <cstring>
#include <set>
#include "ConnectionHandler.h"

#define POLL_SIZE_SEGMENT 50

class ProxyCore
{
public:
    ProxyCore(int socket_fd);
    ~ProxyCore();
    bool isCreated();
    bool listenConnections();

    bool setSocketUnavailableToSend(int socket);
    bool setSocketAvailableToSend  (int socket);
    bool addSocketToPoll(int socket, short events, ConnectionHandler *executor);
private:
    void clearData();
    bool initSocket(int sock_fd);
    bool initPollSet();
    bool initConnectionHandlers();

    int sock = -1;
    size_t poll_size;
    size_t poll_cur_size;
    pollfd *poll_set;
    ConnectionHandler **c_handlers{};

    bool can_work = false;
    bool created = false;

    ssize_t getSocketIndex(int socket);

    void removeClosedSockets(std::set<int> *trashbox);
    void removeSocketByIndex(size_t pos);
    void removeSocket(std::set<int> *trashbox, int socket);
};

#endif
