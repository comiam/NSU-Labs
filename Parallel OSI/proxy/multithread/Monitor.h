#ifndef MULTITHREAD_MONITOR_H
#define MULTITHREAD_MONITOR_H

#include <pthread.h>
#include <cstring>
#include <cstdlib>
#include <semaphore.h>
#include <stdexcept>
#include "errhandle.h"

class Monitor
{
private:
    pthread_mutex_t m_lock = PTHREAD_MUTEX_INITIALIZER;
    pthread_cond_t cv = PTHREAD_COND_INITIALIZER;

    bool locked  = false;
public:
    Monitor();
    ~Monitor();
    bool isLocked() const;
    void wait();
    void notify();
    void notifyAll();
    void lock();
    void unlock();
};

#endif
