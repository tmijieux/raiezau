#ifndef PROTOCOL_H
#define PROTOCOL_H

#include "network.h"

void handle_request(int sock, const struct sockaddr_in *addr);

#endif //PROTOCOL_H
