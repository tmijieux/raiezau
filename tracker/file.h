#ifndef FILE_H
#define FILE_H

#include <stdint.h>
#include "cutil/list.h"

struct file {
    char *filename;
    char *md5_str;
    uint32_t length;

    uint32_t piece_count;
    uint32_t piece_size;

    struct list *clients; // seeders or leechers on this file
};

#include "client.h"

struct file *file_new(
    char *filename, uint32_t length, uint32_t piece_size, char *md5_str);
void file_add_client(struct file *f, struct client *c);

struct file *file_get_by_key(const char *md5_key);
struct file *file_get_by_name(const char *name);

#endif //FILE_H
