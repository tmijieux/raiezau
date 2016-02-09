#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>


struct file {
    off_t st_size;
    uint32_t piece_count;
    char *filename;
    char md5_hash[MD5_DIGEST_SIZE];
    
};
