#include <cerrno>
#include "monitor.h"

Monitor::Monitor()
{
    pthread_mutexattr_t attributes;
    int errcode = pthread_mutexattr_init(&attributes);
    if (NO_ERROR != errcode)
    {
#ifdef DEBUG_ENABLED
        errorfln("Failed to initialize attributes: %s", strerror(errcode));
#endif
        return;
    }

    errcode = pthread_mutexattr_settype(&attributes, PTHREAD_MUTEX_DEFAULT);
    if (NO_ERROR != errcode)
    {
#ifdef DEBUG_ENABLED
        errorfln("Failed to set mutex type");
#endif
        pthread_mutexattr_destroy(&attributes);
        return;
    }

    errcode = pthread_mutex_init(&m_lock, &attributes);
    pthread_mutexattr_destroy(&attributes);

    if (NO_ERROR != errcode)
    {
#ifdef DEBUG_ENABLED
        errorfln("Cannot init mutex: %s", strerror(errcode));
#endif
        return;
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

    created = true;
}

Monitor::~Monitor()
{
    if(created)
    {
#ifdef DEBUG_ENABLED
        int errcode = pthread_mutex_destroy(&m_lock);
        if (NO_ERROR != errcode)
            errorfln("Cannot destroy mutex: %s", strerror(errcode));

        errcode = pthread_cond_destroy(&cv);
        if (NO_ERROR != errcode)
            errorfln("Cannot destroy condition variable: %s", strerror(errcode));
#elif !defined(DEBUG_ENABLED)
        pthread_mutex_destroy(&m_lock);
        pthread_cond_destroy(&cv);
#endif
    }
}

void Monitor::notifyAll()
{
    assertCreated();
    int errcode = pthread_cond_broadcast(&cv);

    if (NO_ERROR != errcode)
    {
#ifdef DEBUG_ENABLED
        errorfln("Failed to notify on monitor: %s", strerror(errcode));
#endif
        exit(EXIT_FAILURE);
    }
}

void Monitor::notify()
{
    assertCreated();
    int errcode = pthread_cond_signal(&cv);

    if (NO_ERROR != errcode)
    {
#ifdef DEBUG_ENABLED
        errorfln("Failed to notifyAll on monitor: %s", strerror(errcode));
#endif
        exit(EXIT_FAILURE);
    }
}

void Monitor::wait()
{
    assertCreated();
    int errcode = pthread_cond_wait(&cv, &m_lock);
    if (NO_ERROR != errcode)
    {
#ifdef DEBUG_ENABLED
        errorfln("Failed to wait on monitor: %s", strerror(errcode));
#endif
        exit(EXIT_FAILURE);
    }
}

void Monitor::lock()
{
    assertCreated();

    int errcode = pthread_mutex_lock(&m_lock);

    if (NO_ERROR != errcode)
    {
#ifdef DEBUG_ENABLED
        errorfln("Failed to lock monitor: %s", strerror(errcode));
#endif
        exit(EXIT_FAILURE);
    }
}

void Monitor::unlock()
{
    int errcode = pthread_mutex_unlock(&m_lock);
    if (NO_ERROR != errcode)
    {
#ifdef DEBUG_ENABLED
        errorfln("Failed to unlock monitor: %s", strerror(errcode));
#endif
        exit(EXIT_FAILURE);
    }
}

void Monitor::assertCreated() const
{
    if(!created)
    {
#ifdef DEBUG_ENABLED
        errorfln("Monitor didn't created!");
#endif
        exit(EXIT_FAILURE);
    }

}
