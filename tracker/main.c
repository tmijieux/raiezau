#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>

#define PORT 1234
#define BUFSIZE 1024

#define CHK(X_) do { if ((X_) < 0) { perror(#X_);       \
            exit(EXIT_FAILURE);} } while(0)
#define CHK2(X_) do { if (NULL == (X_)) { perror(#X_);  \
            exit(EXIT_FAILURE);} } while(0)


static void let_space_be_zero(char *buf)
{
    while (*buf) {
        if (' '  == *buf)
            *buf = 0;
        ++buf;
    }
}

int (*get_request_handler(const char *buf_))(int,const struct sockaddr_in*,char*)
{
    int (*ret)(int,const struct sockaddr_in*,char*);
    
    char *buf = strdup(buf_);
    let_space_be_zero(buf);
    if (ht_get_entry(request_handlers, buf, &ret) < 0) {
        ret = NULL;
    }
    
    return ret;
}

void handle_request(int sock, const struct sockaddr_in *addr)
{
    char buf[10];
    read(sock, buf, 10);
    int (*request_handler)(int,const struct sockaddr_in*,char*)
        = get_request_handler(buf);
    
    if (request_handler(sock, addr, buf) < 0) {
        let_space_be_zero(buf);
        fprintf(stderr, "Handling request %s failed\n", buf);
    }
}

int make_listener_socket(uint32_t addr, uint16_t port, int *sock)
{
    int s;
    struct sockaddr_in si = { 0 };
    int yes[1] = { 1 };
    
    CHK(s = socket(AF_INET, SOCK_STREAM, 0));
    CHK(setsockopt(s, SOL_SOCKET, SO_REUSEADDR, yes, sizeof yes[0]));
    
    si.sin_family = AF_INET;
    si.sin_port = htons(port);
    si.sin_addr.s_addr = addr;
        
    CHK(bind(s, (const struct sockaddr*) &si, sizeof si));
    CHK(listen(s, SOMAXCONN));

    *sock = s;
    return 1;
}

int server_run(uint32_t addr, uint16_t port)
{
    int s;
    make_listener_socket(addr, port, &s);
    
    while (1) {
        int accept_s;
        struct sockaddr_in accept_si = { 0 };
        socklen_t size = sizeof accept_s;
        CHK(accept_s = accept(s,(struct sockaddr*)&accept_si, &size));

        pid_t p = fork();
        switch (p) {
        case -1:
            fprintf(stderr, "fork: %s\nRetrying ...\n", strerror(errno));
            sleep(1);
            continue;
            break;
        case 0:
            handle_request(accept_s, &accept_si);
            exit(EXIT_SUCCESS);
            break;
        default:
            close(accept_s);
            break;
        }
    }
}

int main(int argc, char *argv[])
{
    uint16_t port = PORT; 
    if (argc >= 2) {
        port = atoi(argv[1]);
    }
    
    load_config_file();
    opt_parse_options(argc, argv);
        
    server_run(port);
    return 0;
}
