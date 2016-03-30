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
static struct list *handle_list;
static pthread_mutex_t handle_mutex;
int handled_client_count = 0;

__attribute__((constructor))
static void client_init(void)
{
    clients = ht_create(0, NULL);
}

struct client *client_create(int sock, int slot)
{
    struct client *c = calloc(sizeof*c, 1);
    socklen_t addr_len = sizeof c->addr;
    if (getpeername(sock, &c->addr, &addr_len) < 0) {
        rz_error("getpeername: %s\n", strerror(errno));
        return NULL;
    }

    c->sock = sock;
    c->slot = slot;
    c->files_seed = list_new(0);
    c->files_leech = list_new(0);
#ifdef DEBUG
    char *dbgstr = sockaddr_stringify(&c->addr);
    rz_debug(_("new client connected from %s\n"),  dbgstr);
    free(dbgstr);
#endif
    return c;
}

void client_free(struct client *c)
{
    int i, s = list_size(c->files_seed);
    for (i = 1; i <= s; ++i) {
        struct file *f;
        f = list_get(c->files_seed, i);
        file_remove_client(f, c);
    }
    s = list_size(c->files_leech);
    for (i = 1; i <= s; ++i) {
        struct file *f;
        f = list_get(c->files_leech, i);
        file_remove_client(f, c);
    }

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

struct client *get_client(int sock, int slot)
{
    struct client *c = NULL;
    char *key = int_stringify(sock);
    if (ht_get_entry(clients, key, &c) < 0) {
        c = NULL;
    }
    free(key);
    return c;
}

struct list *client_list(void)
{
    return ht_to_list(clients);
}

static void delete_client(int sock, int slot)
{
    struct client *c = NULL;
    char *socket_str = int_stringify(sock);
    if (ht_get_entry(clients, socket_str, &c) == 0) {
        rz_debug(_("really deleting client %s\n"), client_to_string(c));
        client_free(c);
        ht_remove_entry(clients, socket_str);
    }
    free(socket_str);
}

void add_client_to_pending_list(struct client *c)
{
    pthread_mutex_lock(&handle_mutex);
    rz_debug(_("client: %s\n"), client_to_string(c));
    if (NULL == handle_list)
        handle_list = list_new(0);
    list_add(handle_list, c);
    pthread_mutex_unlock(&handle_mutex);
}

void mark_client_for_deletion(struct client *c)
{
    c->delete = true;
    add_client_to_pending_list(c);
}

void handle_pending_clients(void)
{
    struct list *remaining_list, *tmp;
    if (NULL == handle_list)
        return;

    remaining_list = list_new(0);
    pthread_mutex_lock(&handle_mutex);
    size_t s = list_size(handle_list);
    for (unsigned i = 1; i <= s; ++i) {
        struct client *c;
        c = list_get(handle_list, i);
        if (!c->thread_handling) {
            if (c->delete) {
                rz_debug(_("try to delete client %s\n"), client_to_string(c));
                fds[c->slot].fd= -1;
                close(c->sock);
                delete_client(c->sock, c->slot);
            } else {
                rz_debug(_("client %s rehear %d\n"),
                         client_to_string(c), fds[c->slot].fd);
                fds[c->slot].events = POLLIN;
                fds[c->slot].revents = 0;
                rz_debug(_("slot: %d, nfds: %d\n"), c->slot, nfds);
            }
            -- handled_client_count;
        } else {
            list_add(remaining_list, c);
        }
    }
    tmp = handle_list;
    handle_list = remaining_list;
    pthread_mutex_unlock(&handle_mutex);

    list_free(tmp);
}

#ifdef DEBUG

const char *client_to_string(struct client *c)
{
    return sockaddr_stringify(&c->addr);
}

#endif
