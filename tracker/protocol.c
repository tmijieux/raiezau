/* protocol.c -- code for handling our P2P protocol */

#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <pthread.h>
#include <limits.h>

#include "rz.h"
#include "file.h"
#include "client.h"
#include "util.h"

#include "cutil/string2.h"
#include "cutil/hash_table.h"
#include "cutil/error.h"

typedef int (*req_handler_t)(struct client*, char*);

int prot_announce(struct client *c, char *req_value);
int prot_update(struct client *c, char *req_value);
int prot_look(struct client *c, char *req_value);
int prot_getfile(struct client *c, char *req_value);

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
    if (write(sock, "ko\n", 4) != 4)
        rz_error("ko : bad write");
    close(sock);
    pthread_exit(NULL);
}

static int read_request(int sock, char **req_name, char **req_value)
{
    int len, ret;
    char *client_req = NULL;

    // read request
    ret = socket_read_string(sock, &client_req);
    if (ret <= 0) {
        free(client_req);
        rz_debug("debug check point 1\n");
        cancel_request_negative_answer(sock);
    }

    rz_debug("Received request: '%s'\n", client_req);

    int deb;
    if ((deb = sscanf(client_req, " %ms %n", req_name, &len)) != 1) {
        free(client_req);
        rz_debug("debug check point 2 : val : %d\n", deb);
        cancel_request_negative_answer(sock);
    }

    *req_value = strdup(client_req + len);
    free(client_req);
    return 0;
}

static void get_and_call_handler(
    struct client *c, char *req_name, char *req_value)
{
    req_handler_t request_handler;
    // get handler
    request_handler = get_request_handler(req_name);
    if (NULL == request_handler) {
        free(req_name); free(req_value);
        cancel_request_negative_answer(c->sock);
    }

    // call handler
    if (request_handler(c, req_value) < 0) {
        rz_error("Handling request `%s´ failed\n", req_name);
    }
    free(req_name); free(req_value);
}

void handle_request(struct client *c)
{
    char *req_name = NULL, *req_value = NULL;

    read_request(c->sock, &req_name, &req_value);
    get_and_call_handler(c, req_name, req_value);
    close(c->sock);
}

int parse_leech_string(struct client *c, char **split, int i, int n);
int parse_seed_string(struct client *c, char **split, int i, int n);

struct list *get_seed_file_list(const char *seed_str)
{
    int n;
    char **tab;
    
    n = string_split(seed_str, " ", &tab);
    if (n % 4 != 0) {
        rz_error("Invalid parameter count in seed string\n");
        return list_new(0);
    }

    struct list *l = list_new(0);
    for (int i = 0; i < n; i+=4) {
        struct file *f = file_get_or_create(
            tab[i],
            atoi(tab[i+1]),
            atoi(tab[i+2]),
            tab[i+3]
        );
        list_append(l, f);
        
        free(tab[i]); free(tab[i+1]); free(tab[i+2]); free(tab[i+3]); 
    }
    free(tab);

    return l;
}

struct list *get_leech_file_list(const char *leech_str)
{
    int n;
    char **tab;
    
    n = string_split(leech_str, " ", &tab);
    
    struct list *l = list_new(0);
    for (int i = 0; i < n; i++) {
        struct file *f = file_get_by_key(tab[i]);
        list_append(l, f);
        free(tab[i]);
    }
    free(tab);

    return l;
}

// parse the element from regexp subexpression
// defined in function 'prot_announce'.
void announce_match_and_parse(struct client *c, char *req, regmatch_t pmatch[])
{
    c->listening_port = atoi(req+pmatch[1].rm_so);

    char *seed_str = strndup(req+pmatch[2].rm_so,
                             pmatch[2].rm_eo - pmatch[2].rm_so);
    char *leech_str = strndup(req+pmatch[3].rm_so,
                             pmatch[3].rm_eo - pmatch[3].rm_so);

    c->files_seed = get_seed_file_list(seed_str);
    c->files_leech = get_leech_file_list(leech_str);

    
    for (int i = 1; i <= list_size(c->files_seed); ++i)
        file_add_client(list_get(c->files_seed, i), c);
    for (int i = 1; i <= list_size(c->files_seed); ++i)
        file_add_client(list_get(c->files_leech, i), c);

    free(seed_str);
    free(leech_str);
}

int prot_announce(struct client *c, char *req_value)
{
    int ret;
    char *regexp;
    regmatch_t match[4];

    regexp = "listen ([1-9]{1,5}) seed \\[(.*)\\] leech \\[(.*)\\]";
    ret = regex_exec(regexp, req_value, 4, match);
    if (ret < 0) return -1;
    announce_match_and_parse(c, req_value, match);
    
    if (write(c->sock, "ok\n", 4) != 4)
        rz_error("bad write: %s\n", strerror(errno));
    return 0;
}

int prot_update(struct client *c, char *req_value)
{
    if (write(c->sock, "update ok\n", 11) != 11)
        rz_error("bad write: %s\n", strerror(errno));
    close(c->sock);
    return 0;
}

int prot_look(struct client *c, char *req_value)
{
    if (write(c->sock, "look ok\n", 9) != 9)
        rz_error("bad write: %s\n", strerror(errno));
    close(c->sock);
    return 0;
}

int prot_getfile(struct client *c, char *req_value)
{
    if (write(c->sock, "getfile ok\n", 12) != 12)
        rz_error("bad write: %s\n", strerror(errno));
    close(c->sock);
    return 0;
}
