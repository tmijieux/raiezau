#ifndef CLIENT_H
#define CLIENT_H

#include <stdint.h>
#include <stdbool.h>
#include "rz.h"
#include "network.h"
#include "cutil/list.h"

struct conn_state {
    bool announced;
};

struct client {
    int socket;
    struct sockaddr_in addr;
    char *conn_addr_key;

    uint16_t listening_port;
    char *listen_addr_key;

    struct list *files_seed;
    struct list *files_leech;

    int ref_count;
    int can_rehear;
    pthread_t current_thr;
    struct conn_state state;
};

struct client *client_get_or_create(int socket);
struct client *client_get(const char *conn_key);

struct list *client_list(void);
void client_set_listening_port(struct client *c, uint16_t port);

void client_inc_ref(struct client *c);
void client_dec_ref(struct client *c);

const char *client_to_string(struct client *c);

#endif //CLIENT_H
