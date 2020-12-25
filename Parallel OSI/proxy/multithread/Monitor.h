#ifndef MONITOR_H
#define MONITOR_H

#include <pthread.h>
#include <cstring>
#include <cstdlib>
#include <semaphore.h>
#include "errhandle.h"

class Monitor
{
private:
    pthread_mutex_t m_lock = PTHREAD_MUTEX_INITIALIZER;
    pthread_cond_t cv = PTHREAD_COND_INITIALIZER;

    bool created = false;
    bool locked  = false;

    void assertCreated() const;
public:
    Monitor();
    ~Monitor();
    bool isLocked();
    void wait();
    void notify();
    void notifyAll();
    void lock();
    void unlock();
};

#endif
