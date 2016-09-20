
#include "./ServerSocket.hpp"


ServerSocket::ServerSocket(int sock):
    Socket(sock)
{
}

ServerSocket::ServerSocket(string addr, uint16_t port):
    Socket(Socket::makeListener(addr, port))
{
}

bool ServerSocket::eventHandler(uint32_t /*events*/)
{
    int s;
    while ((s = accept(_sock)) >= 0)
        _readBuf->write(&s, sizeof s);
    int save_errno = errno;

    if (!acceptHandler())
        return false;

    errno = save_errno;
    if (errno != EAGAIN && errno != EWOULDBLOCK)
        return false;
    return true;
}
