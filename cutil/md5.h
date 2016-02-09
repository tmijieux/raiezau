#ifndef MD5_H
#define MD5_H

#include <stdio.h>
#include <stdlib.h>
#include <openssl/md5.h>

int md5_hash_file(FILE *f, unsigned char *md5_hash_str);
int md5_str_hash_file(FILE *f, char **md5_hash_str);
int md5_hash_buf(
    size_t size, const unsigned char *buf, unsigned char *md5_hash);
int md5_str_hash_buf(size_t size, const unsigned char *buf, char **ret_str);

#endif //MD5_H
