#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <pthread.h>

#include "rz.h"
#include "file.h"
#include "cutil/string2.h"
#include "cutil/hash_table.h"
#include "cutil/error.h"
#include "client.h"

typedef int (*req_handler_t)(struct client*, int n, char **split);

int prot_announce(struct client*, int n, char **split);
int prot_update(struct client*, int n, char **split);
int prot_look(struct client*, int n, char **split);
int prot_getfile(struct client*, int n, char **split);

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

__no_return
static void cancel_request_negative_answer(int sock)
{
    write(sock, "ko\n", 4);
    close(sock);
    pthread_exit(NULL);
}

static void free_strings(int n, char **split)
{
    if (NULL != split) {
        for (int i = 0; i < n; ++i)
            free(split[i]);
        free(split);
    }
}

static int read_and_split_request(int sock, char ***splitted)
{
    int tok_c, ret;
    char *client_req = NULL;

    // read request
    ret = socket_read_string(sock, &client_req);
    if (ret <= 0) {
        free(client_req);
        rz_debug("debug check point 1\n");
        cancel_request_negative_answer(sock);
    }

    // split request
    tok_c = string_split(client_req, " ", splitted);
    if (tok_c <= 0) {
        free(client_req);
        rz_debug("debug check point 2\n");
        cancel_request_negative_answer(sock);
    }
    free(client_req);
    return tok_c;
}

static void get_and_call_handler(struct client *c, int n, char **splitted)
{
    req_handler_t request_handler;
    // get handler
    request_handler = get_request_handler(splitted[0]);
    if (NULL == request_handler) {
        free_strings(n, splitted);
        cancel_request_negative_answer(c->sock);
    }
    
    // call handler
    if (request_handler(c, n, splitted) < 0) {
        fprintf(stderr, "Handling request `%s´ failed\n", splitted[0]);
    }
    free_strings(n, splitted);
}

void handle_request(struct client *c)
{
    char **split_req = NULL;
    int tok_c;

    tok_c = read_and_split_request(c->sock, &split_req);
    get_and_call_handler(c, tok_c, split_req);
}

int parse_leech_string(struct client *c, char **split, int i, int n);
int parse_seed_string(struct client *c, char **split, int i, int n);

int prot_announce(struct client *c, int n, char **split)
{
    int i = 1;
    while (i < n) {
        if (!strcmp("listen", split[i])) {
            if (i == n-1) {
                rz_error("bugubugu\n");
                break;
            }
            
            c->listening_port = (uint16_t) atoi(split[i+1]);
            i += 2;
            continue;
        }

        if (!strcmp("seed", split[i])) {
            i = parse_seed_string(c, split, i, n);
            continue;
        }

        if (!strcmp("seed", split[i])) {
            i = parse_leech_string(c, split, i, n);
            continue;
        }

        rz_debug("unexpected announce argument `%s´\n", split[i]);
    }
    
    if (write(c->sock, "ok\n", 4) != 4)
        rz_error("bad write: %s\n", strerror(errno));
    close(c->sock);
    return 0;
}

int prot_update(struct client *c, int n, char **split)
{
    if (write(c->sock, "update ok\n", 11) != 11)
        rz_error("bad write: %s\n", strerror(errno));
    close(c->sock);
    return 0;
}

int prot_look(struct client *c, int n, char **split)
{
    if (write(c->sock, "look ok\n", 9) != 9)
        rz_error("bad write: %s\n", strerror(errno));
    close(c->sock);
    return 0;
}

int prot_getfile(struct client *c, int n, char **split)
{
    if (write(c->sock, "getfile ok\n", 12) != 12)
        rz_error("bad write: %s\n", strerror(errno));
    close(c->sock);
    
    return 0;
}

int parse_seed_string(struct client *c, char **split, int i, int n)
{
    int s;
    if (!strcmp("[", split[i]))
        ++i;

    s = i;
    while (s < n && !character_is_in_string(']', split[s]))
        ++s;
    
    if (!strcmp("]", split[s]))
        --s;
    
    if (s-i % 4 != 0) {
        rz_error("invalid format for announce seed list"
                 " (must have multiple of four parameters")
    }

    for (int a = i; a < s; a+=4) {
        struct file *f;
        f = file_new(split[a],
                     atoi(split[a+1]),
                     atoi(split[a+2]),
                     split[a+3]);

        file_add_client(f, c);
    }
        
    return s+1;
}

int parse_leech_string(struct client *c, char **split, int i, int n)
{
    int s;
    if (!strcmp("[", split[i]))
        ++i;

    s = i;
    while (s < n && !character_is_in_string(']', split[s]))
        ++s;
    
    if (!strcmp("]", split[s]))
        --s;
    
    for (int a = i; a < s; ++a) {
        struct file *f;
        f = file_get_by_key(split[i]);
        if (NULL != f) {
            file_add_client(f, c);
        } else {
            rz_error("Invalid file key `%s´\n", split[i]);
        }
    }
        
    return s+1;
}
