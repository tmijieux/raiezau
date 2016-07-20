#include <fcntl.h>
#include <sys/epoll.h>
#include <unistd.h>

#include "../containers/CircularBuffer.hpp"
#include "../error/error.hpp"
#include "../EventManager.hpp"
#include "./SocketWriter.hpp"

SocketWriter::SocketWriter(int sock, Buffer *b, EventManager &eventManager):
    Event(sock, EPOLLOUT | EPOLLONESHOT),
    _sfd(sock),
    _buf(b),
    _armed(true),
    _eventManager(eventManager)
{
}

SocketWriter::~SocketWriter()
{
    ::close(_sfd);
}

bool SocketWriter::eventHandler(uint32_t /*events*/)
{
    _armed = false;
    size_t bsize = _buf->size();
    if (bsize == 0)
        return true;
    
    int r;
    if ((r = _buf->writeToFd(_sfd)) > 0 && (size_t) r < bsize)
        this->rearm();
    if (r == 0 || (r < 0 && errno != EAGAIN && errno != EWOULDBLOCK)) {
        rz_debug("writer error\n");
        if (r==0)
            rz_debug("r == 0\n");
        else
            rz_debug("%s\n", strerror(errno));
        return false;
    }
    return true;
}

void SocketWriter::rearm()
{
    if (_armed)
        return;
    _eventManager.rearmEvent(this);
    _armed = true;
}

int SocketWriter::makeSockFD(int sock)
{
    int s =  dup(sock);
    EventManager::makeNonBlockingFD(sock);
    int flags = fcntl(s, F_GETFL, 0);
    fcntl(s, F_SETFL, flags | O_NONBLOCK);
    return s;
}
