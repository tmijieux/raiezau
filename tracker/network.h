#ifndef NETWORK_H
#define NETWORK_H

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>

char *sockaddr_stringify(const struct sockaddr_in *si);
int make_listener_socket(uint32_t addr, uint16_t port, int *sock);
void server_run_bind_any(uint16_t port);
void server_run(uint32_t addr, uint16_t port);

#endif //NETWORK_H
