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

typedef int (*req_handler_t)(struct client*, char **split);

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

req_handler_t get_request_handler(const char *buf_c)
{
    char *tmp, *buf;
    req_handler_t ret;
    
    tmp = str_replace_char(buf_c, ' ', '\0');
    buf = str_replace_char(tmp, '\n', '\0');
    free(tmp);
    
    if (ht_get_entry(request_handlers, buf, &ret) < 0) {
        rz_error("cannot get handler with key `%s´\n", buf);
        ret = NULL;
    }
    free(buf);
    return ret;
}

__attribute__((noreturn))
static void cancel_request_negative_answer(struct client *c)
{
    
    write(c->sock, "ko\n", 4);
    close(c->sock);
    pthread_exit(NULL);
}

static void free_strings(char *req, int n, char **split)
{
    free(req);
    if (split)
        for (int i = 0; i < n; ++i)
            free(split[i]);
    free(split);
}

void handle_request(struct client *c)
{
    char *client_req = NULL;
    char **split_req = NULL;
    int tok_c = 0, ret;
    req_handler_t request_handler;
    
    ret = socket_read_string(c->sock, &client_req);
    if (ret <= 0) {
        free_strings(client_req, tok_c, split_req);
        cancel_request_negative_answer(c);
    }
    
    tok_c = string_split(client_req, " ", &split_req);
    if (tok_c <= 0) {
        free_strings(client_req, tok_c, split_req);
        cancel_request_negative_answer(c);
    }

    request_handler = get_request_handler(split_req[0]);
    if (NULL == request_handler) {
        free_strings(client_req, tok_c, split_req);
        cancel_request_negative_answer(c);
    }

    if (request_handler(c, split_req) < 0) {
        fprintf(stderr, "Handling request `%s´ failed\n", split_req[0]);
    }

    free_strings(client_req, tok_c, split_req);
}

int prot_announce(struct client *c)
{
    if (write(c->sock, "annouce ok\n", 12) != 12)
        rz_error("bad write: %s\n", strerror(errno));
    close(c->sock);
    return 0;
}

int prot_update(struct client *c)
{
    if (write(c->sock, "update ok\n", 11) != 11)
        rz_error("bad write: %s\n", strerror(errno));
    close(c->sock);
    return 0;
}

int prot_look(struct client *c)
{
    if (write(c->sock, "look ok\n", 9) != 9)
        rz_error("bad write: %s\n", strerror(errno));
    close(c->sock);
    return 0;
}

int prot_getfile(struct client *c)
{
    if (write(c->sock, "getfile ok\n", 12) != 12)
        rz_error("bad write: %s\n", strerror(errno));
    close(c->sock);
    
    return 0;
}
