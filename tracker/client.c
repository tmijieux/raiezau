#include <stdint.h>
#include <stdlib.h>

#include "cutil/hash_table.h"

struct client {
    int sock;
    struct sockaddr_in *addr;
    uint16_t listening_port;
    struct list *files;
};

static struct hash_table *clients;

__attribute__((constructor))
static void client_init(void)
{
    clients = ht_create(0, NULL);
}

struct client *get_or_create_client(const struct sockaddr_in *si)
{

    return NULL;
}

void set_client_sockaddr(
    struct client *c, int sock, const struct sockaddr_in *si)
{

}
