#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <pthread.h>
#include <errno.h>

#include "client.h"
#include "util.h"
#include "cutil/error.h"
#include "cutil/hash_table.h"

static struct hash_table *clients;

__attribute__((constructor))
static void client_init(void)
{
    clients = ht_create(0, NULL);
}

struct client *client_create(int sock, int slot)
{
    socklen_t sl;
    struct client *c = calloc(sizeof*c, 1);
    if (getpeername(sock, &c->addr, &sl) < 0) {
        rz_error("getpeername: %s\n", strerror(errno));
        exit(EXIT_FAILURE);
    }
    c->sock = sock;
    c->slot = slot;
#ifdef DEBUG
    char *dbgstr = sockaddr_stringify(&c->addr);
    rz_debug(_("new client connected from %s\n"),  dbgstr);
    free(dbgstr);
#endif
    return c;
}

void client_free(struct client *c)
{
    free(c);
}

struct client *get_or_create_client(int sock, int slot)
{
    struct client *c = NULL;
    char *key = int_stringify(sock);
    if (ht_get_entry(clients, key, &c) < 0) {
        c = client_create(sock, slot);
        ht_add_entry(clients, key, c);
    }
    free(key);
    return c;
}

struct list *client_list(void)
{
    return ht_to_list(clients);
}

void delete_client(int sock, int slot)
{
    struct client *c = NULL;
    char *socket_str = int_stringify(sock);
    if (ht_get_entry(clients, socket_str, &c) < 0) {
        client_free(c);
        ht_remove_entry(clients, socket_str);
    } 
    free(socket_str);
}

