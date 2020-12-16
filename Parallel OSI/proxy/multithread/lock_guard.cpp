
#include "lock_guard.h"

lock_guard::lock_guard(pthread_mutex_t *mutex): mutex(mutex)
{
    pthread_mutex_lock(mutex);
}

lock_guard::~lock_guard()
{
    pthread_mutex_unlock(mutex);
}
