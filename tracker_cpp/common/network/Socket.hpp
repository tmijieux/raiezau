#ifndef SOCKET_H
#define SOCKET_H

class Socket;

#include <stdlib.h>
#include <stdint.h>
#include <netinet/ip.h>

#include "../containers/Buffer.hpp"
#include "../EventManager.hpp"

class Socket : public Event {
    friend class ServerSocket;
    friend class ClientSocket;
    
public:
    enum SocketOption {
        SO_TCP_NODELAY,
    };
    Socket(int sock);
    
    void setOption(enum SocketOption option);
    virtual uint32_t getLocalIP() const;
    
    virtual std::string to_string() const;
    virtual void close();
    virtual ~Socket();
    
    static int makeListener(string addr, uint16_t port);
    static int connect(string addr, uint16_t port);
    static int accept(int listener);
    
protected:
    Buffer *_readBuf;
    
private:
    int _sock;
    struct sockaddr_in _localAddr;
};

#endif //SOCKET_H
