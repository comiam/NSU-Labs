#ifndef GUARD_H
#define GUARD_H

#include <mutex>

//with blackjack and conditional variables
class lock_guard
{
public:
    lock_guard(pthread_mutex_t *mutex);
    ~lock_guard();
private:
    pthread_mutex_t *mutex;
};


#endif
