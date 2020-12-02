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
        printf("Invalid arg size! Expected: <port num>\n");
        return EXIT_FAILURE;
    }

    int port = atoi(argv[1]);

    sockaddr_in addr;
    addr.sin_family = AF_INET;
    addr.sin_port = htons(port);
    addr.sin_addr.s_addr = htonl(INADDR_ANY);

    int sock = socket(AF_INET, SOCK_STREAM, 0);

    if(sock == -1)
    {
        perror("Can't create server socket");
        return EXIT_FAILURE;
    }

    if (bind(sock, (sockaddr *) &addr, sizeof(addr)) == -1)
    {
        perror("Can't bind socket on port");
        close(sock);
        return EXIT_FAILURE;
    }

    try
    {
        proxy = new ProxyCore(sock);
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

    printf("Proxy started on port: %d\n", port);

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

