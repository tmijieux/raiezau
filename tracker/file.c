#include <stdio.h>
#include <stdlib.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <assert.h>

#include "cutil/hash_table.h"

#include "file.h"

static struct hash_table *file_by_hash;

__attribute__ ((constructor))
static void file_init(void)
{
    file_by_hash = ht_create(0, NULL);

#ifdef DEBUG
    file_new("test1", 10000, 1024, "deadcafebeefbabe");
#endif
}

static void file_register(const struct file *f)
{
    assert( NULL != f );

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
    f->piece_count = (length / piece_size) + ((length%piece_size) ? 1 : 0);

    file_register(f);

    return f;
}

void file_add_client(struct file *f, struct client *c)
{
    list_add(f->clients, c);
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
