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
#include "network.h"
#include "file.h"

static struct hash_table *clients;


__attribute__((constructor))
static void client_init(void)
{
    clients = ht_create(0, NULL);
}

struct client *client_create(
    int socket, const struct sockaddr_in *addr, const char *conn_key)
{
    struct client *c = calloc(sizeof*c, 1);

    c->addr = *addr;
    c->conn_addr_key = strdup(conn_key);
    c->listen_addr_key = NULL;
    c->socket = socket;

    c->files_seed = list_new(0);
    c->files_leech = list_new(0);
    c->ref_count = 0;

#ifdef DEBUG
    char *dbgstr = sockaddr_stringify(&c->addr);
    rz_debug(_("new client connected from %s\n"),  dbgstr);
    free(dbgstr);
#endif

    return c;
}

void client_set_listening_port(struct client *c, uint16_t port)
{
    char *ip;
    c->listening_port = port;

    ip = ip_stringify(c->addr.sin_addr.s_addr);
    asprintf(&c->listen_addr_key, "%s:%hu", ip, port);
    free(ip);
}

static void remove_client_from_file_list(
    struct client *c, struct list *files)
{
    int i, s;
    s = list_size(files);
    for (i = 1; i <= s; ++i) {
        struct file *f;
        f = list_get(files, i);
        file_remove_client(f, c);
    }
}

static void client_free(struct client *c)
{
    remove_client_from_file_list(c, c->files_seed);
    list_free(c->files_seed);
    remove_client_from_file_list(c, c->files_leech);
    list_free(c->files_leech);

    free(c->conn_addr_key);
    free(c->listen_addr_key);

    free(c);
}

struct client *client_get_or_create(int socket)
{
    struct client *c = NULL;
    struct sockaddr_in addr;
    socklen_t addr_len = sizeof addr;
    char *conn_key;

    if (getpeername(socket, &addr, &addr_len) < 0) {
        rz_error("getpeername: %s\n", strerror(errno));
        return NULL;
    }
    conn_key = sockaddr_stringify(&addr);

    if (ht_get_entry(clients, conn_key, &c) < 0) {
        c = client_create(socket, &addr, conn_key);
        ht_add_entry(clients, conn_key, c);
    }
    free(conn_key);
    return c;
}

struct client *client_get(const char *conn_key)
{
    struct client *c = NULL;
    if (ht_get_entry(clients, conn_key, &c) < 0) {
        return NULL;
    }
    return c;
}

struct list *client_list(void)
{
    return ht_to_list(clients);
}

static void delete_client(struct client *c)
{
    rz_debug(_("really deleting client %s\n"), client_to_string(c));
    ht_remove_entry(clients, c->conn_addr_key);
    client_free(c);
}

void client_inc_ref(struct client *c)
{
    ++ c->ref_count;
}

void client_dec_ref(struct client *c)
{
    -- c->ref_count;
    if (c->ref_count <= 0)
        delete_client(c);
}

#ifdef DEBUG
const char *client_to_string(struct client *c)
{
    return c->conn_addr_key;
}
#endif
