#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <assert.h>

#include "cutil/hash_table.h"

struct file {
    char *filename;
    char *md5_str;
    off_t st_size;
    
    uint32_t piece_count;
    uint32_t piece_size;
    
    struct list *clients; // seeders or leechers on this file
};

struct hash_table *file_by_name;
struct hash_table *file_by_hash;

__attribute__ ((constructor))
static void file_init(void)
{
    file_by_name = ht_create(0, NULL);
    file_by_hash = ht_create(0, NULL);
}

void file_add(const struct file *f)
{
    assert( NULL != f );
    
    ht_add_entry(file_by_name, f->filename, (void*) f);
    ht_add_entry(file_by_hash, f->md5_str, (void*) f);
}
