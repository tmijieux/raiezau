#include <stdint.h>

struct client {
    struct sockaddr_in *addr;
    uint16_t listening_port;
    
    struct list *files;
};
