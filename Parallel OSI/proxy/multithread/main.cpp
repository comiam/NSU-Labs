#include <csignal>
#include "ProxyCore.h"

void sig_handler(int sig);
pthread_t thread;

int main(int argc, char *argv[])
{
    if (argc != 2)
    {
        printf("Invalid arg size! Expected: <port number>\n");
        return EXIT_FAILURE;
    }

    int port = atoi(argv[1]);

    sigset(SIGPIPE, SIG_IGN);
    sigset(SIGSTOP, SIG_IGN);
    sigset(SIGTERM, &sig_handler);
    sigset(SIGINT, &sig_handler);

    thread = initProxyCore(port);
    if(thread)
        pthread_join(thread, nullptr);

    return 0;
}

void sig_handler(int signum)
{
    pthread_cancel(thread);
}

