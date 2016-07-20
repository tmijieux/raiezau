#ifndef SERVERSOCKET_H
#define SERVERSOCKET_H

class ServerSocket;

#include "./Socket.hpp"

class ServerSocket : public Socket {
public:
    ServerSocket(int sock);
    ServerSocket(string addr, uint16_t port);
    
protected:
    virtual bool eventHandler(uint32_t events) override final;
    virtual bool acceptHandler() = 0;
};

#endif //SERVERSOCKET_H
