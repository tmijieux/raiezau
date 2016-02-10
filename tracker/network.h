#ifndef NETWORK_H
#define NETWORK_H

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>

char *sockaddr_stringify(const struct sockaddr_in *si);
char *ip_stringify(uint32_t ip_addr);

void server_run_bind_any_addr(uint16_t port);
void server_run(uint32_t addr, uint16_t port);

#endif //NETWORK_H
