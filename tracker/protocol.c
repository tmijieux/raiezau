/* protocol.c -- code for handling our P2P protocol */

#define _GNU_SOURCE
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
#include "cutil/md5.h"

typedef int (*req_handler_t)(struct client*, char*);

static int prot_announce(struct client *c, char *req_value);
static int prot_update(struct client *c, char *req_value);
static int prot_look(struct client *c, char *req_value);
static int prot_getfile(struct client *c, char *req_value);

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

static req_handler_t get_request_handler(const char *buf_c)
{
    char *tmp, *buf;
    req_handler_t ret;

    tmp = str_replace_char(buf_c, ' ', '\0');
    buf = str_replace_char(tmp, '\n', '\0');
    free(tmp);

    if (ht_get_entry(request_handlers, buf, &ret) < 0) {
        rz_error(_("cannot get handler with key `%s´\n"), buf);
        ret = NULL;
    }
    free(buf);
    return ret;
}

static void negative_answer(int sock)
{
    socket_write_string(sock, 3, "ko\n");
}

static void write_ok(int sock)
{
    socket_write_string(sock, 3, "ok\n");
}

static int read_request(int sock, char **req_name, char **req_value)
{
    int rd, ret = 0;
    char *client_req = NULL;

    // read request
    rd = socket_read_string(sock, &client_req);
    if (rd > 0) {
        int len;
        rz_debug(_("Received request: '%s'\n"), client_req);

        int nmatch;
        if ((nmatch = sscanf(client_req, " %ms %n", req_name, &len)) != 1) {
            negative_answer(sock);
            ret = -1;
        } else {   // sscanf == 1
            ret = rd;
            *req_value = strdup(client_req + len);
        }
    } else { // read_string <= 0
        negative_answer(sock);
        ret = -1;
    }
    free(client_req);
    return ret;
}

static void get_and_call_handler(
    struct client *c, char *req_name, char *req_value)
{
    req_handler_t request_handler;
    // get handler
    request_handler = get_request_handler(req_name);
    if (NULL != request_handler) {
        // call handler
        if (request_handler(c, req_value) < 0) {
            rz_error(_("Handling request `%s´ failed\n"), req_name);
            negative_answer(c->sock);
        }
    } else {
        negative_answer(c->sock);
    }
    free(req_name); free(req_value);
}

// this is the entry point
void handle_request(struct client *c)
{
    char *req_name = NULL, *req_value = NULL;

    if (read_request(c->sock, &req_name, &req_value) > 0) {
        get_and_call_handler(c, req_name, req_value);
    }
    close(c->sock);
}

static struct list *get_seed_file_list(const char *seed_str)
{
    int n;
    char **tab;

    n = string_split(seed_str, " ", &tab);
    if (n % 4 != 0) {
        rz_error(_("Invalid parameter count in seed string\n"));
        for (int i = 0; i < n; i++) free(tab[i]);
        free(tab);
        return list_new(0);
    }

    struct list *l = list_new(0);
    for (int i = 0; i < n; i+=4) {
        struct file *f = file_get_or_create(
            tab[i],         // filename
            atol(tab[i+1]), // file size (byte)
            atoi(tab[i+2]), // piece_size
            tab[i+3]        // key (md5)
        );
        list_append(l, f);
        free(tab[i]); free(tab[i+1]); free(tab[i+2]); free(tab[i+3]);
    }
    free(tab);

    return l;
}

static struct list *get_leech_file_list(const char *leech_str)
{
    int n;
    char **tab = NULL;

    n = string_split(leech_str, " ", &tab);

    struct list *l = list_new(0);
    for (int i = 0; i < n; i++) {
        struct file *f = file_get_by_key(tab[i]);
        if (NULL != f)
            list_append(l, f);
        else
            rz_debug(_("invalid file key %s\n"), tab[i]);

        free(tab[i]);
    }
    free(tab);
    return l;
}

static void seed_leech_match_and_parse(
    struct client *c, char *req, regmatch_t pmatch[])
{
    char *seed_str = strndup(
        req+pmatch[1].rm_so, pmatch[1].rm_eo - pmatch[1].rm_so);
    char *leech_str = strndup(
        req+pmatch[2].rm_so, pmatch[2].rm_eo - pmatch[2].rm_so);

    list_free(c->files_seed);
    list_free(c->files_leech);

    c->files_seed = get_seed_file_list(seed_str);
    c->files_leech = get_leech_file_list(leech_str);

    {
        typedef void (*fun_t)(void*,void*);
        list_each_r(c->files_seed, (fun_t) file_add_client, c);
        list_each_r(c->files_leech, (fun_t) file_add_client, c);
    }

    free(seed_str);
    free(leech_str);
}

// parse the element from regexp subexpression
// defined in function 'prot_announce'.
static void announce_match_and_parse(
    struct client *c, char *req, regmatch_t pmatch[])
{
    c->listening_port = atoi(req+pmatch[1].rm_so);
    pmatch[1] = pmatch[0];
    seed_leech_match_and_parse(c, req, &pmatch[1]);
}

// parse the element from regexp subexpression
// defined in function 'prot_update'.
static void update_match_and_parse(
    struct client *c, char *req, regmatch_t pmatch[])
{
    seed_leech_match_and_parse(c, req, pmatch);
}

static int prot_announce(struct client *c, char *req_value)
{
    int ret;
    char *regexp;
    regmatch_t match[4];

    regexp = "listen ([1-9]{1,5}) seed \\[(.*)\\] leech \\[(.*)\\] *";
    ret = regex_exec(regexp, req_value, 4, match);
    if (ret < 0)
        return -1;
    announce_match_and_parse(c, req_value, match);
    write_ok(c->sock);
    return 0;
}

static int prot_update(struct client *c, char *req_value)
{
    int ret;
    char *regexp;
    regmatch_t match[3];

    regexp = "seed \\[(.*)\\] leech \\[(.*)\\] *";
    ret = regex_exec(regexp, req_value, 3, match);
    if (ret < 0)
        return -1;
    update_match_and_parse(c, req_value, match);
    write_ok(c->sock);
    return 0;
}

static void prot_look_process_criterions(
    struct client *c, const char *criterion)
{
    // split + for loop
}

static int prot_look(struct client *c, char *req_value)
{
    int mc;
    char *str = NULL;
    mc = sscanf(req_value, " [%m[^]]] ", &str); // viva el scanf powa
    if (mc == 1){
        prot_look_process_criterions(c, str);
    }
    free(str);
    write_ok(c->sock);
    return 0;
}

static void str_trim_prot_getfile(char *req)
{
    while (*req != '\0' && *req != '\n')
        ++req;
    *req = '\0';
}

static char *client_endpoints_string_list(struct list *cli)
{
    char *out, *tmp = "";
    unsigned len;

    if (cli == NULL || (len = list_size(cli)) == 0)
        return strdup("");

    for (unsigned i = 1; i <= len; ++i) {
        struct client *c = list_get(cli, i);
        char *addr = ip_stringify(c->addr.sin_addr.s_addr);
        asprintf(&out, "%s%s%s:%hd", tmp, i > 1 ? " " : "",
                 addr, c->listening_port);
        free(addr);
        if (i > 1) free(tmp);
        tmp = out;
    }
    return out;
}

static int prot_getfile(struct client *c, char *req_value)
{
    size_t len;
    struct file *f;
    char *response, *endpoints_list;

    str_trim_prot_getfile(req_value);
    len = strlen(req_value);
    if (len != KEYHASH_STRSIZE) {
        rz_debug(_("Invalid key size\n"));
        return -1;
    }

    f = file_get_by_key(req_value);
    if (NULL == f) {
        rz_debug(_("No such file key: '%s'\n"), req_value);
        return -1;
    }

    endpoints_list = client_endpoints_string_list(f->clients);
    len = asprintf(&response, "peers %s [%s]", req_value, endpoints_list);
    socket_write_string(c->sock, len, response);

    free(response);
    free(endpoints_list);

    return 0;
}
