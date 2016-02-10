#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <pthread.h>
#include <errno.h>

#include "client.h"

#include "cutil/error.h"
#include "cutil/hash_table.h"

static struct hash_table *clients;

__attribute__((constructor))
static void client_init(void)
{
    clients = ht_create(0, NULL);
}

struct client *client_create(const struct sockaddr_in *si)
{
    struct client *c = calloc(sizeof*c, 1);
    c->addr = *si;
    rz_debug("new client connection from %s\n", sockaddr_stringify(si));
    return c;
}

struct client *get_or_create_client(const struct sockaddr_in *si)
{
    struct client *c;
    char *socket_str = sockaddr_stringify(si);
    if (ht_get_entry(clients, socket_str, &c) < 0) {
        c = client_create(si);
        ht_add_entry(clients, socket_str, c);
    }
    
    return c;
}

void set_client_sockaddr(
    struct client *c, int sock, const struct sockaddr_in *si)
{
    c->sock = sock;
    c->addr = *si;
}
