#ifndef CLIENT_H
#define CLIENT_H

#include <stdint.h>
#include <stdbool.h>
#include "rz.h"
#include "network.h"
#include "cutil/list.h"

extern int handled_client_count;

struct conn_state {
    bool announced;
};
    
struct client {
    int sock;
    int slot;

    bool delete;
    bool thread_handling;
    
    struct sockaddr_in addr;

    uint16_t listening_port;
    
    struct list *files_seed;
    struct list *files_leech;

    pthread_t current_thr;
    struct conn_state state;
};

struct client *get_or_create_client(int sock, int slot);
struct client *get_client(int sock, int slot);
struct list *client_list(void);

void mark_client_for_deletion(struct client *c);
void add_client_to_pending_list(struct client *c);
void handle_pending_clients(void);
const char *client_to_string(struct client *c);


#endif //CLIENT_H
