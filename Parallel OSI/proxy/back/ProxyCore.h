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
    bool created = false;
    bool closing = false;
    int proxy_socket = -1;
    int port = 0;
    int poll_call_fd[2];
    char poll_call_byte = 0;

    //Constructor and destructor functions
    void clearData();
    bool initSocket(int sock_fd);
    bool initPollSet();
    static void unlockMonitor(Monitor monitor);

    //poll thread functions
    friend void *poll_routine(void *args);
    bool addSocketToPoll(int socket, short events, ConnectionHandler *executor);
    void removeHandlerImpl(int socket);
    bool addServerConnections();
    bool addClientConnection();
    void deleteDeadConnections();

    //proxy thread functions
    void addNewTask(int poll_position, int revent);
    void pushNewPollEvent(int pos, int revent);
    void mergeAndPushNewTaskToLists();
    void freeBusySockets();
    void notifyConveyor();
    ssize_t getSocketIndex(int _sock);

    //proxy child threads
    pthread_t poll_thread;
    std::vector<pthread_t> thread_pool;

    //used in poll thread
    int poll_close_flag;
    std::vector<pollfd> poll_set;
    std::vector<int> trash_set;
    std::vector<std::pair<int, ConnectionHandler*>> new_server_set;
    std::map<int, ConnectionHandler*> socketHandlers;

    //task containers
    std::map<int, bool> busy_set;
    std::vector<int> free_set;
    std::map<int, int> handler_task_list;
    std::map<int, int> queued_tasks;
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
    } conveyor_task_list;

    class PollTaskList: public std::vector<std::pair<int, int>>, public Monitor
    {
    public:
        PollTaskList(): std::vector<std::pair<int, int>>(), Monitor() {}
    } poll_task_list;

    Monitor add_lock;
    Monitor remove_lock;

    Monitor free_lock;
    Monitor htask_lock;
    Monitor proxy_task_lock;
};

#endif
