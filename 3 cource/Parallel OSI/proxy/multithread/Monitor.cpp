
#include "Monitor.h"

Monitor::Monitor()
{
    pthread_mutexattr_t attributes;
    int errcode = pthread_mutexattr_init(&attributes);
    if (NO_ERROR != errcode)
    {
#ifdef DEBUG_ENABLED
        errorfln("Failed to initialize attributes: %s", strerror(errcode));
#endif
        throw std::runtime_error("Can't init monitor!");
    }

    errcode = pthread_mutexattr_settype(&attributes, PTHREAD_MUTEX_DEFAULT);
    if (NO_ERROR != errcode)
    {
#ifdef DEBUG_ENABLED
        errorfln("Failed to set mutex type");
#endif
        pthread_mutexattr_destroy(&attributes);
        throw std::runtime_error("Can't init monitor!");
    }

    errcode = pthread_mutexattr_setprotocol(&attributes, PTHREAD_PRIO_INHERIT);
    if (NO_ERROR != errcode)
    {
#ifdef DEBUG_ENABLED
        errorfln("Failed to set mutex type");
#endif
        pthread_mutexattr_destroy(&attributes);
        throw std::runtime_error("Can't init monitor!");
    }

    errcode = pthread_mutex_init(&m_lock, &attributes);
    pthread_mutexattr_destroy(&attributes);

    if (NO_ERROR != errcode)
    {
#ifdef DEBUG_ENABLED
        errorfln("Cannot init mutex: %s", strerror(errcode));
#endif
        throw std::runtime_error("Can't init monitor!");
    }

    errcode = pthread_cond_init(&cv, nullptr);
    if (NO_ERROR != errcode)
    {
#ifdef DEBUG_ENABLED
        errorfln("Cannot init condition variable: %s", strerror(errcode));
#endif
        pthread_mutex_destroy(&m_lock);
        return;
    }
}

Monitor::~Monitor()
{
#ifdef DEBUG_ENABLED
        int errcode = pthread_mutex_destroy(&m_lock);
        if (NO_ERROR != errcode)
            errorfln("Cannot destroy mutex: %s", strerror(errcode));

        errcode = pthread_cond_destroy(&cv);
        if (NO_ERROR != errcode)
            errorfln("Cannot destroy condition variable: %s", strerror(errcode));
#elif !defined(DEBUG_ENABLED)
        locked = false;
        pthread_mutex_destroy(&m_lock);
        pthread_cond_destroy(&cv);
#endif
}

void Monitor::notifyAll()
{
    int errcode = pthread_cond_broadcast(&cv);

    if (NO_ERROR != errcode)
    {
#ifdef DEBUG_ENABLED
        errorfln("Failed to notify on monitor: %s", strerror(errcode));
#endif
        throw std::runtime_error(strerror(errcode));
    }
}

void Monitor::notify()
{
    int errcode = pthread_cond_signal(&cv);
    if (NO_ERROR != errcode)
    {
#ifdef DEBUG_ENABLED
        errorfln("Failed to notifyAll on monitor: %s", strerror(errcode));
#endif
        throw std::runtime_error(strerror(errcode));
    }
}

void Monitor::wait()
{
    locked = false;
    int errcode = pthread_cond_wait(&cv, &m_lock);
    if (NO_ERROR != errcode)
    {
#ifdef DEBUG_ENABLED
        errorfln("Failed to wait on monitor: %s", strerror(errcode));
#endif
        throw std::runtime_error(strerror(errcode));
    }else
        locked = true;
}

void Monitor::lock()
{
    int errcode = pthread_mutex_lock(&m_lock);

    if (NO_ERROR != errcode)
    {
#ifdef DEBUG_ENABLED
        errorfln("Failed to poll_lock monitor: %s", strerror(errcode));
#endif
        throw std::runtime_error(strerror(errcode));
    }else
        locked = true;
}

void Monitor::unlock()
{
    int errcode = pthread_mutex_unlock(&m_lock);
    if (NO_ERROR != errcode)
    {
#ifdef DEBUG_ENABLED
        errorfln("Failed to unlock monitor: %s", strerror(errcode));
#endif
        throw std::runtime_error(strerror(errcode));
    }else
        locked = false;
}

bool Monitor::isLocked() const
{
    return locked;
}
