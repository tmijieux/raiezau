#ifndef NETWORK_H
#define NETWORK_H

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <poll.h>

#include "rz.h"

extern pthread_t network_thread;
extern int nfds;
extern uint32_t fds_buffer_size;
extern struct pollfd *fds;

char *sockaddr_stringify(const struct sockaddr_in *si);
char *ip_stringify(uint32_t ip_addr);

void server_run_bind_any_addr(uint16_t port);
void server_run(uint32_t addr, uint16_t port);

int socket_read_string(int sock, char **ret_str);
void socket_write_string(int sock, size_t len, const char *str);

#endif //NETWORK_H
