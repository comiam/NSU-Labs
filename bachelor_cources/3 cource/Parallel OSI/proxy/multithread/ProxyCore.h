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
#include <vector>
#include <algorithm>
#include <unordered_map>
#include <list>
#include "Monitor.h"
#include "ConnectionHandler.h"

#define POLL_SIZE_SEGMENT 100
#define CLOSE_SIGNAL -1

pthread_t initProxyCore(std::tuple<int, int, int*> &port);

class ProxyCore
{
public:
    ProxyCore(int port, int thread_count);
    ~ProxyCore();

    bool isClosed() const;
    bool isCreated() const;
    bool listenConnections();
    int getProxyNotifier();

    void lockHandlers();
    void unlockHandlers();

    ConnectionHandler * getHandlerBySocket(int socket);
    std::pair<int, int> getTask();
    void                madeSocketFreeForPoll(int sock);
    void                setSocketUnavailableToSend(int socket);
    void                setSocketAvailableToSend  (int socket);
    bool                addSocketToPollQueue(int socket, ConnectionHandler *executor);
    void                removeHandler(int sock);
    void                noticePoll();

    static void closeSocket(int sock, bool is_server_sock);
private:
    bool initSocket(int sock_fd);
    bool initPollSet();

    bool    addSocketToPoll(int socket, short events, ConnectionHandler *executor);
    void    removeHandlerImpl(int sock);
    ssize_t getSocketIndex(int _sock);

    void markPollSockets();
    bool addServerConnections();
    bool addClientConnection();
    void handleBusyConnections();
    void deleteDeadConnections();
    void addNewTask(int sock, int revent);

    static void unlockMonitor(Monitor monitor);

    char pipe_data = 0;
    int poll_pipe[2] = {0,0};
    int proxy_socket = -1;
    int port = 0;

    pthread_t cache_timer_thread;
    std::vector<pthread_t> thread_pool;
    std::vector<pollfd> poll_set;
    std::vector<int> trash_set;
    std::vector<int> free_set;
    std::vector<std::pair<int, ConnectionHandler*>> new_server_set;

    std::unordered_map<int, bool> sock_rdwr;
    std::unordered_map<int, bool> busy_set;
    std::unordered_map<int, ConnectionHandler*> socket_handlers;
    std::unordered_map<int, int> socket_positions;

    std::list<int> task_order;
    class TaskSet: public std::unordered_map<int, int>, public Monitor
    {
    public:
        TaskSet(): std::unordered_map<int, int>(), Monitor() {}
    } task_list;

    Monitor add_lock;
    Monitor free_lock;
    Monitor remove_lock;
    Monitor rdwr_lock;

    bool created = false;
    bool closed = false;
    bool have_marked_connections = false;
};

#endif
