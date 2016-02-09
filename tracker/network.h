#ifndef NETWORK_H
#define NETWORK_H

int make_listener_socket(uint32_t addr, uint16_t port, int *sock);
void server_run_bind_any(uint16_t port);
void server_run(uint32_t addr, uint16_t port);

#endif //NETWORK_H
