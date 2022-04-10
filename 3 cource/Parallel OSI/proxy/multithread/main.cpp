#include "ProxyCore.h"
#include <csignal>

void sig_handler(int sig);
int  proxy_notifier = -1;

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

    auto arg = std::make_tuple(port, count, &proxy_notifier);
    pthread_t thread = initProxyCore(arg);

    if(thread)
        pthread_join(thread, nullptr);//we may do anything this thread, come on))

    return 0;
}

void sig_handler(int signum)
{
    if(proxy_notifier == -1)
        printf("Interesting shit happened on signal %i...\n", signum);
    else
    {
        char close_answer = CLOSE_SIGNAL;
        write(proxy_notifier, &close_answer, sizeof(char));
    }
}

