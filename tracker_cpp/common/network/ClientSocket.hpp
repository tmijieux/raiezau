#ifndef CLIENTSOCKET_H
#define CLIENTSOCKET_H

class ClientSocket;

#include <cstdint>
#include "./Socket.hpp"
#include "./SocketWriter.hpp"

class ClientSocket : public Socket {
public:
    ClientSocket(int sock, EventManager &eventManager);
    ClientSocket(string addr, uint16_t port, EventManager &eventManager);
    ~ClientSocket();

    void write(void *data, size_t size);
    void write(uint8_t *data, size_t size);
    void write(std::string msg);

    virtual uint32_t getRemoteIP() const;
protected:
    virtual bool readHandler() = 0;

private:
    virtual bool eventHandler(uint32_t events) override final;

    Buffer *_writeBuf;
    SocketWriter *_sockWriter;
    EventManager &_eventManager;
    struct sockaddr_in _remoteAddr;
};

#endif //CLIENTSOCKET_H
