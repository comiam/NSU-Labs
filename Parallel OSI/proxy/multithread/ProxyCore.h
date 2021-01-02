#ifndef MULTITHREAD_PROXYCORE_H
#define MULTITHREAD_PROXYCORE_H

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
#include "Monitor.h"
#include "ConnectionHandler.h"

#define POLL_SIZE_SEGMENT 100

pthread_t initProxyCore(std::pair<int, int> &port);

class ProxyCore
{
public:
    ProxyCore(int port, int thread_count);
    ~ProxyCore();

    static void closeSocket(int sock, bool is_server_sock);
    bool isCreated() const;
    bool listenConnections();

    void lockHandlers();
    void unlockHandlers();
    void madeSocketFreeForPoll(int sock);

    void setSocketUnavailableToSend(int socket);
    void setSocketAvailableToSend  (int socket);
    bool addSocketToPollQueue(int socket, ConnectionHandler *executor);

    void                removeHandler(int sock);
    std::pair<int, int> getTask();

    ConnectionHandler *getHandlerBySocket(int socket);
private:
    void clearData();
    bool initSocket(int sock_fd);
    bool initPollSet();

    bool addSocketToPoll(int socket, short events, ConnectionHandler *executor);

    void removeHandlerImpl(int sock);
    void markPollSockets();
    bool addServerConnections();
    bool addClientConnection();
    void freeBusyConnections();
    void deleteDeadConnections();
    void removeBusyConnectionsFromPoll();
    void addNewTask(int poll_position, int revent);

    static void unlockMonitor(Monitor monitor);

    int proxy_socket = -1;
    int port = 0;
    std::vector<pthread_t> thread_pool;
    std::vector<pollfd> poll_set;
    std::vector<int> trash_set;
    std::vector<std::pair<int, bool>> sock_rdwr;
    std::vector<int> free_set;
    std::vector<std::pair<int, ConnectionHandler*>> new_server_set;
    std::vector<pollfd> trashbox;
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

    Monitor add_lock;
    Monitor free_lock;
    Monitor remove_lock;
    Monitor rdwr_lock;

    bool created = false;
    bool closing = false;
    bool have_marked_connections = false;

    ssize_t getSocketIndex(int _sock);
};

#endif
