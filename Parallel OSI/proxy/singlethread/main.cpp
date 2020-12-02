#include <netinet/in.h>
#include <sys/socket.h>
#include <unistd.h>
#include <csignal>
#include "ProxyCore.h"

void sig_handler(int sig);

ProxyCore *proxy;

int main(int argc, char *argv[])
{
    if (argc != 2)
    {
        printf("Invalid arg size! Expected: <port number>\n");
        return EXIT_FAILURE;
    }

    int port = atoi(argv[1]);

    try
    {
        proxy = new ProxyCore(port);
    } catch (std::bad_alloc &e)
    {
        perror("Can't init proxy");
        return EXIT_FAILURE;
    }

    if(!proxy->isCreated())
    {
        perror("Can't init proxy");
        return EXIT_FAILURE;
    }

    sigset(SIGPIPE, SIG_IGN);
    sigset(SIGSTOP, SIG_IGN);
    sigset(SIGINT, &sig_handler);

    if (proxy->listenConnections())
    {
        printf("Proxy closed with error!\n");
        return EXIT_FAILURE;
    }

    delete(proxy);
    return 0;
}

void sig_handler(int signum)
{
    proxy->closeProxy();
}

