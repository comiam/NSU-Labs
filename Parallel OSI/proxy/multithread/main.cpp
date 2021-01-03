#include "ProxyCore.h"
#include <csignal>

void sig_handler(int sig);
pthread_t thread;

int main(int argc, char *argv[])
{
    if (argc != 3)
    {
        printf("Invalid arg size! Expected: <port number> <worker_thread_count>\n");
        return EXIT_FAILURE;
    }

    //84.237.52.20
    int port = atoi(argv[1]);
    int count = atoi(argv[2]);

    sigset(SIGPIPE, SIG_IGN);
    sigset(SIGTERM, &sig_handler);
    sigset(SIGINT, &sig_handler);

    auto arg = std::make_pair(port, count);
    thread = initProxyCore(arg);
    if(thread)
        pthread_join(thread, nullptr);

    return 0;
}

void sig_handler(int signum)
{
    pthread_cancel(thread);
}

