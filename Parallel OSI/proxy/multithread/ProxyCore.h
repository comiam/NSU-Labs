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
#include <queue>
#include "ConnectionHandler.h"

#define POLL_SIZE_SEGMENT 50

pthread_t initProxyCore(std::pair<int, int> &port);

class ProxyCore
{
public:
    ProxyCore(int port, int thread_count);
    ~ProxyCore();

    void closeSocket(int sock) const;
    bool isCreated() const;
    bool listenConnections();

    void setSocketUnavailableToSend(int socket);
    void setSocketAvailableToSend  (int socket);
    bool addSocketToPoll(bool from_proxy_thread, int socket, short events, ConnectionHandler *executor);

    void                 clearHandler(int sock);
    std::pair<int, int>  getTask();

    ConnectionHandler *getHandlerBySocket(int socket);
private:
    void clearData();
    bool initSocket(int sock_fd);
    bool initPollSet();

    int sock = -1;
    int port = 0;
    std::vector<pthread_t> thread_pool;
    std::vector<pollfd> poll_set;
    std::queue<std::pair<int, int>> task_queue;
    std::set<int> task_list;
    std::map<int, ConnectionHandler*> socketHandlers;

    pthread_mutex_t core_mutex = PTHREAD_MUTEX_INITIALIZER;
    pthread_mutex_t task_mutex = PTHREAD_MUTEX_INITIALIZER;
    pthread_cond_t  task_cond  = PTHREAD_COND_INITIALIZER;

    bool created = false;

    ssize_t getSocketIndex(int _sock);

    void removeSocket(size_t _sock);
};

#endif
