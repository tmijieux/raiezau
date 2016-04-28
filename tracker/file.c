#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#include <stdio.h>
#include <stdlib.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <assert.h>
#include <string.h>

#include "cutil/initializer.h"
#include "cutil/hash_table.h"
#include "cutil/error.h"

#include "file.h"

static struct hash_table *file_by_hash;
static struct hash_table *file_by_name;

INITIALIZER(file_init)
{
    file_by_hash = ht_create(0, NULL);
    file_by_name = ht_create(0, NULL);

    #ifdef DEBUG
    file_new("test1", 10000, 1024, "deadcafebeefbabe");
    file_new("test2", 5000, 512, "8905e92afeb80fc7722ec89eb0bf0966");
    #endif
}

static void file_register(const struct file *f)
{
    assert( NULL != f );
    ht_add_entry(file_by_hash, f->md5_str, (void*) f);

    struct list *l;
    if (ht_get_entry(file_by_name, f->filename, &l) < 0) {
        l = list_new(0);
        ht_add_entry(file_by_name, f->filename, l);
    }
    list_add(l, f);
}

struct file *file_new(
    char *filename, uint32_t length, uint32_t piece_size, char *md5_str)
{
    struct file *f = calloc(sizeof*f, 1);
    f->filename = strdup(filename);
    f->md5_str = strdup(md5_str);
    f->length = length;
    f->piece_size = piece_size;
    f->piece_count = (length / piece_size) + ((length%piece_size) ? 1 : 0);
    f->clients = ht_create(0, NULL);

    file_register(f);
    return f;
}

void file_add_client(struct file *f, struct client *c)
{
    ht_add_unique_entry(f->clients, c->listen_addr_key, c);
}

void file_remove_client(struct file *f, struct client *c)
{
    ht_remove_entry(f->clients, c->listen_addr_key);
}

static struct file *file_get_by_(struct hash_table *ht, const char *what)
{
    struct file *f = NULL;
    if (ht_get_entry(ht, what, &f) < 0) {
        f = NULL;
    }
    return f;
}

struct file *file_get_by_key(const char *md5_key)
{
    return file_get_by_(file_by_hash, md5_key);
}

struct list *file_get_by_name(const char *name)
{
    struct list *l = NULL;
    if (ht_get_entry(file_by_name, name, &l) < 0)
        return NULL;
    return l;
}

struct file *file_get_or_create(
    char *filename, uint32_t length, uint32_t piece_size, char *md5_str)
{
    struct file *f = file_get_by_key(md5_str);
    if (NULL == f) {
        f = file_new(filename, length, piece_size, md5_str);
    }
    return f;
}

struct list *file_list(void)
{
    return ht_to_list(file_by_hash);
}

void file_print_peer_list(struct file *f)
{
    if (ht_entry_count(f->clients) == 0) {
        printf("%s: No peers\n", f->filename);
        return;
    }

    bool first = true;
    char *str = strdup("");
    void append_peer(const char *n, void *c_, void *ctx)
    {
        char *tmp = str;
        struct client *c = c_;
        asprintf(&str, "%s%s%s", str, first?"":" | ", c->listen_addr_key);
        first = false;
        free(tmp);
    }
    ht_for_each(f->clients, &append_peer, NULL);
    printf("%s|%s: [%s]\n", f->filename, f->md5_str, str);
    free(str);
}
