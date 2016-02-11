#include <stdio.h>
#include <stdlib.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <assert.h>

#include "cutil/hash_table.h"

#include "file.h"

static struct hash_table *file_by_name;
static struct hash_table *file_by_hash;

__attribute__ ((constructor))
static void file_init(void)
{
    file_by_name = ht_create(0, NULL);
    file_by_hash = ht_create(0, NULL);
}

static void file_register(const struct file *f)
{
    assert( NULL != f );

    ht_add_entry(file_by_name, f->filename, (void*) f);
    ht_add_entry(file_by_hash, f->md5_str, (void*) f);
}

struct file *file_new(
    char *filename, uint32_t length, uint32_t piece_size, char *md5_str)
{
    struct file *f = calloc(sizeof*f, 1);
    f->filename = filename;
    f->md5_str = md5_str;
    f->length = length;
    f->piece_size = piece_size;

    file_register(f);

    return f;
}

void file_add_client(struct file *f, struct client *c)
{
    list_add(f->clients, c);
}
