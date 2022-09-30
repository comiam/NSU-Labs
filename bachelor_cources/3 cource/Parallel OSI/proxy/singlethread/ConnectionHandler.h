#ifndef SINGLETHREAD_HANDLER_H
#define SINGLETHREAD_HANDLER_H

class ConnectionHandler
{
public:
    virtual bool execute(int event) = 0;
    virtual ~ConnectionHandler() = default;

    virtual bool sendData() = 0;
    virtual bool receiveData() = 0;
};

#endif //SINGLETHREAD_HANDLER_H
