#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <pthread.h>

#include "cutil/string2.h"
#include "cutil/hash_table.h"
#include "cutil/error.h"
#include "client.h"

typedef int (*req_handler_t)(struct client*);

int prot_announce(struct client*);
int prot_update(struct client*);
int prot_look(struct client*);
int prot_getfile(struct client*);

static struct hash_table *request_handlers;

__attribute__((constructor))
static void protocol_init(void)
{
    request_handlers = ht_create(0, NULL);

    ht_add_entry(request_handlers, "announce", &prot_announce);
    ht_add_entry(request_handlers, "update", &prot_update);
    ht_add_entry(request_handlers, "look", &prot_look);
    ht_add_entry(request_handlers, "getfile", &prot_getfile);
}

req_handler_t get_request_handler(const char *buf)
{
    req_handler_t ret;
    buf = str_replace_char(buf, ' ', '\0');
    buf = str_replace_char(buf, '\n', '\0');
    
    if (ht_get_entry(request_handlers, buf, &ret) < 0) {
        rz_error("cannot get handler with key %s\n", buf);
        ret = NULL;
    }
    return ret;
}

void handle_request(struct client *c)
{
    char buf[11] = {0};
    read(c->sock, buf, 10);
    req_handler_t request_handler = get_request_handler(buf);
    if (NULL == request_handler) {
        write(c->sock, "ko\n", 4);
        close(c->sock);
        pthread_exit(NULL);
    }

    if (request_handler(c) < 0) {
        char *buf2 = str_replace_char(buf, ' ', '\0');
        fprintf(stderr, "Handling request %s failed\n", buf2);
        free(buf2);
    }
}

int prot_announce(struct client *c)
{
    write(c->sock, "annouce ok\n", 12);
    close(c->sock);

    return 0;
}

int prot_update(struct client *c)
{
    write(c->sock, "update ok\n", 11);
    close(c->sock);

    return 0;
}

int prot_look(struct client *c)
{
    write(c->sock, "look ok\n", 9);
    close(c->sock);

    return 0;
}

int prot_getfile(struct client *c)
{
    write(c->sock, "getfile ok\n", 12);
    close(c->sock);
    
    return 0;
}


    
