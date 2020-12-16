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
#include "ConnectionHandler.h"

#define POLL_SIZE_SEGMENT 50

pthread_t initProxyCore(int &port);

class ProxyCore
{
public:
    ProxyCore(int port);
    ~ProxyCore();

    void closeSocket(int sock) const;
    bool isCreated() const;
    bool listenConnections();

    void setSocketUnavailableToSend(int socket);
    void setSocketAvailableToSend  (int socket);
    bool addSocketToPoll(int socket, short events, ConnectionHandler *executor);

    ConnectionHandler *getHandlerBySocket(int socket);
private:
    void clearData();
    bool initSocket(int sock_fd);
    bool initPollSet();

    int sock = -1;
    int port = 0;
    std::vector<pollfd> poll_set;
    std::map<int, ConnectionHandler*> socketHandlers;

    pthread_mutex_t core_mutex = PTHREAD_MUTEX_INITIALIZER;

    bool created = false;

    ssize_t getSocketIndex(int _sock);

    void removeClosedSockets(std::set<int> *trashbox);
    void removeSocket(size_t _sock);
};

#endif
