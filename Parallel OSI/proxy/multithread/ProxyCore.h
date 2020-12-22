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
#include <map>
#include <vector>
#include <queue>
#include <unordered_set>
#include <algorithm>
#include "monitor.h"
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

    void lockHandlers();
    void unlockHandlers();
    void madeSocketFreeForPoll(int sock);

    void setSocketUnavailableToSend(int socket);
    void setSocketAvailableToSend  (int socket);
    bool addSocketToPoll(int socket, short events, ConnectionHandler *executor);
    bool addSocketToPollWithoutBlocking(int socket, short events, ConnectionHandler *executor);

    void                removeHandler(int sock);
    std::pair<int, int> getTask();

    ConnectionHandler *getHandlerBySocket(int socket);
private:
    void clearData();
    bool initSocket(int sock_fd);
    bool initPollSet();

    int sock = -1;
    int port = 0;
    std::vector<pthread_t> thread_pool;
    std::vector<pollfd> poll_set;
    std::map<int, pollfd> busy_set;
    std::map<int, ConnectionHandler*> socketHandlers;

    struct pair_hash
    {
        inline std::size_t operator()(const std::pair<int,int> & v) const
        {
            return v.first*31+v.second;
        }
    };
    class TaskSet: public std::unordered_set<std::pair<int, int>, pair_hash>, public Monitor
    {
    public:
        TaskSet(): std::unordered_set<std::pair<int, int>, pair_hash>(), Monitor() {}
    } task_list;

    //Monitor lock;
    sem_t lock;

    bool created = false;
    bool closing = false;

    ssize_t getSocketIndex(int _sock);
};

#endif
