#ifndef FILE_H
#define FILE_H

#include <stdint.h>
#include "cutil/hash_table.h"
#include "cutil/list.h"

struct file {
    char *filename;
    char *md5_str;
    uint64_t length;

    uint32_t piece_count;
    uint32_t piece_size;

    struct hash_table *clients; // seeders or leechers on this file
};

#include "client.h"

#define file_size(f) ((f)->length)

struct file *file_new(
    char *filename, uint32_t length, uint32_t piece_size, char *md5_str);
void file_add_client(struct file *f, struct client *c);
void file_remove_client(struct file *f, struct client *c);

struct file *file_get_by_key(const char *md5_key);
struct file *file_get_by_name(const char *name);

struct file *file_get_or_create(
    char *filename, uint32_t length, uint32_t piece_size, char *md5_str);

struct list *file_list(void);


#endif //FILE_H
