#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <sys/epoll.h>
#include <arpa/inet.h>
#include <errno.h>
#include <fcntl.h>

#include <sstream>
#include "./Socket.hpp"
#include "../error/error.hpp"
#include "../containers/CircularBuffer.hpp"

#define CHK(mesg, X_) do { if ((X_) < 0) { perror(#mesg); \
            exit(EXIT_FAILURE);} } while(0)

#define SOCK_BUF_SIZE 32768

static std::string sockaddr_to_string(const struct sockaddr_in *si)
{
    std::stringstream ss;
    char addr[20] = {0};
    inet_ntop(AF_INET, &si->sin_addr.s_addr, addr, 19);

    ss << addr << ":" << ntohs(si->sin_port);
    return ss.str();
}

std::string Socket::to_string() const
{
    return sockaddr_to_string(&_localAddr);
}

int Socket::connect(string address, uint16_t port)
{
    struct sockaddr_in sin;
    int s_;
    CHK(socket, s_ = socket(AF_INET, SOCK_STREAM, 0));

    sin.sin_family = AF_INET;
    sin.sin_port = htons(port);
    inet_aton(address.c_str(), &sin.sin_addr);

    CHK(connect, ::connect(s_, (const struct sockaddr*) &sin, sizeof sin));
    return s_;
}

int Socket::makeListener(string addr, uint16_t port)
{
    int s;
    struct sockaddr_in si;
    int yes[1] = { 1 };

    memset(&si, 0, sizeof si);
    CHK(socket, s = socket(AF_INET, SOCK_STREAM, 0));
    CHK(setsockopt, setsockopt(
        s, SOL_SOCKET, SO_REUSEADDR, yes, sizeof yes[0]));

    si.sin_family = AF_INET;
    si.sin_port = htons(port);
    inet_aton(addr.c_str(), &si.sin_addr);

    CHK(bind, bind(s, (const struct sockaddr*) &si, sizeof si));
    CHK(listen, listen(s, SOMAXCONN));
    rz_debug("server listening on socket %s\n", sockaddr_to_string(&si).c_str());

    return s;
}

int Socket::accept(int listener)
{
    struct sockaddr_in accept_si;
    socklen_t size = sizeof accept_si;
    memset(&accept_si, 0, sizeof accept_si);
    return ::accept(listener, (struct sockaddr*)&accept_si, &size);
}

Socket::Socket(int sock):
    Event(sock, EPOLLIN | EPOLLET),
    _readBuf(new CircularBuffer(SOCK_BUF_SIZE)),
    _sock(sock)
{
    socklen_t len = sizeof _localAddr;
    CHK(getsockname, getsockname(_sock, (struct sockaddr*) &_localAddr, &len));
}

uint32_t Socket::getLocalIP() const
{
    return _localAddr.sin_addr.s_addr;
}

void Socket::setOption(enum SocketOption option)
{
    switch (option) {
    case SO_TCP_NODELAY:
    {
        int yes[1] = {1};
        socklen_t len = sizeof yes;
        CHK(setsockopt, setsockopt(_sock, IPPROTO_TCP, TCP_NODELAY, yes, len));
        break;
    }
    default:
        break;
    }
}

void Socket::close()
{
    Event::scheduleUnregister();
}

Socket::~Socket()
{
    delete _readBuf;
    ::close(_sock);
}
