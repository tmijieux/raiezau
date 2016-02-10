#ifndef CLIENT_H
#define CLIENT_H

struct client;

struct client *get_client(const struct sockaddr_in *si);
void set_client_sockaddr(
    struct client *c, int sock, const struct sockaddr_in *si);


#endif //CLIENT_H
