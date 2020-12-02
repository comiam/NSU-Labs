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
#include <map>
#include <vector>
#include <algorithm>
#include "ConnectionHandler.h"

#define POLL_SIZE_SEGMENT 50

class ProxyCore
{
public:
    ProxyCore(int socket_fd);
    ~ProxyCore();

    void closeProxy();
    void closeSocket(int sock) const;
    bool isCreated();
    bool listenConnections();

    bool setSocketUnavailableToSend(int socket);
    bool setSocketAvailableToSend  (int socket);
    bool addSocketToPoll(int socket, short events, ConnectionHandler *executor);

    ConnectionHandler *getHandlerBySocket(int socket);
private:
    void clearData();
    bool initSocket(int sock_fd);
    bool initPollSet();

    int sock = -1;
    std::vector<pollfd> poll_set;
    std::map<int, ConnectionHandler*> socketHandlers;

    bool can_work = false;
    bool created = false;

    ssize_t getSocketIndex(int _sock);

    void removeClosedSockets(std::set<int> *trashbox);
    void removeSocket(size_t _sock);
};

#endif
