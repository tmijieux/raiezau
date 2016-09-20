#include <unistd.h>

#include "../error/error.hpp"
#include "../containers/CircularBuffer.hpp"
#include "./ClientSocket.hpp"

#define CHK(mesg, X_) do { if ((X_) < 0) { perror(#mesg); \
            exit(EXIT_FAILURE);} } while(0)

#define BUF_SIZE 1024
#define SOCK_BUF_SIZE 32768

ClientSocket::ClientSocket(int sock, EventManager &eventManager):
    Socket(sock),
    _writeBuf(new CircularBuffer(SOCK_BUF_SIZE)),
    _sockWriter(NULL),
    _eventManager(eventManager)
{
    socklen_t len = sizeof _remoteAddr;
    CHK(getpeername, getpeername(_sock, (struct sockaddr*) &_remoteAddr, &len));
}

ClientSocket::ClientSocket(string addr, uint16_t port, EventManager &eventManager):
    Socket(Socket::connect(addr, port)),
    _writeBuf(new CircularBuffer(SOCK_BUF_SIZE)),
    _sockWriter(NULL),
    _eventManager(eventManager)
{
    socklen_t len = sizeof _remoteAddr;
    CHK(getpeername, getpeername(_sock, (struct sockaddr*) &_remoteAddr, &len));
}

ClientSocket::~ClientSocket()
{
    delete _writeBuf;
    if (_sockWriter != NULL) {
        _eventManager.unregisterEvent(_sockWriter);
        delete _sockWriter;
    }
}

uint32_t ClientSocket::getRemoteIP() const
{
    return _remoteAddr.sin_addr.s_addr;
}

bool ClientSocket::eventHandler(uint32_t /*events*/)
{
    ssize_t len;
    char buf[BUF_SIZE];

    while ((len = ::read(_sock, buf, BUF_SIZE)) > 0) {
        while ((size_t) len > _readBuf->remainingSpace())
            _readBuf->resize(1.5);
        _readBuf->write(buf, len);
    }
    int save_errno = errno;

    if (!readHandler())
        return false;

    errno = save_errno;
    return (len != 0 && (errno == EAGAIN || errno == EWOULDBLOCK));
}

void ClientSocket::write(uint8_t *data, size_t size)
{
    if (size == 0)
        return;

    while (size > _writeBuf->remainingSpace())
        _writeBuf->resize(1.5);
    _writeBuf->write(data, size);

    if (_sockWriter != NULL) {
        _sockWriter->rearm();
        return;
    }
    _sockWriter = new SocketWriter(
        SocketWriter::makeSockFD(_sock), _writeBuf, _eventManager);
    _eventManager.registerEvent(_sockWriter);
}

void ClientSocket::write(void *data, size_t size)
{
    write((uint8_t*) data, size);
}

void ClientSocket::write(std::string msg)
{
    write((uint8_t*) msg.c_str(), msg.length());
}
