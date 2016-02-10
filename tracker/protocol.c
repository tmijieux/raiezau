#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>

#include "cutil/string2.h"
#include "cutil/hash_table.h"
#include "client.h"


typedef int (*req_handler_t)(struct client*);

int prot_announce(struct client *);
int prot_look(struct client *);
int prot_getfile(struct client *);

static struct hash_table *request_handlers;


__attribute__((constructor))
static void protocol_init(void)
{
    request_handlers = ht_create(0, NULL);

    ht_add_entry(request_handlers, "announce", &prot_announce);
    ht_add_entry(request_handlers, "update", &prot_announce);
    ht_add_entry(request_handlers, "look", &prot_look);
    ht_add_entry(request_handlers, "getfile", &prot_getfile);
}

req_handler_t get_request_handler(const char *buf_)
{
    req_handler_t ret;
    char *buf = str_replace_char(buf_, ' ', '\0');
    if (ht_get_entry(request_handlers, buf, &ret) < 0)
        ret = NULL;
    return ret;
}

void handle_request(struct client *c)
{
    char buf[10];
    read(c->sock, buf, 10);
    int (*request_handler)(int,const struct sockaddr_in*,char*)
        = get_request_handler(buf);

    if (request_handler(sock, addr, buf) < 0) {
        char *buf = str_replace_char(buf, ' ', '\0');
        fprintf(stderr, "Handling request %s failed\n", buf);
        free(buf);
    }
}


int prot_announce(struct client *c, int request_size, char **request)
{

    return 0;
}

int prot_update(struct client *c, int request_size, char **request)
{

    return 0;
}

int prot_look(struct client *c, int request_size, char **request)
{

    return 0;
}

int prot_getfile(struct client *c, int request_size, char **request)
{

    return 0;
}
