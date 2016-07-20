#include <cstdlib>
#include <cstdio>

#include <fcntl.h>
#include <sys/epoll.h>
#include <unistd.h>

#include "./EventManager.hpp"
#include "../common/error/error.hpp"

#define CHK(mesg, X_) do { if ((X_) < 0) { perror(#mesg); \
            exit(EXIT_FAILURE);} } while(0)

void EventManager::unregisterEvent(Event *ev)
{
    struct epoll_event epoll_ev;
    epoll_ctl(_epollHandle, EPOLL_CTL_DEL, ev->_fd, &epoll_ev);
    _events.erase(ev->_fd);
    if (ev->_autoDelete)
        _deleteSet.insert(_deleteSet.begin(), ev);
    -- _eventCount;
}

void EventManager::rearmEvent(Event *ev)
{
    struct epoll_event epoll_ev;
    memset(&epoll_ev, 0, sizeof epoll_ev);

    epoll_ev.events = ev->_events;
    epoll_ev.data.fd = ev->_fd;
    epoll_ctl(_epollHandle, EPOLL_CTL_MOD, ev->_fd, &epoll_ev);
}


void EventManager::makeNonBlockingFD(int fd)
{
    int flags = fcntl(fd, F_GETFL, 0);
    fcntl(fd, F_SETFL, flags | O_NONBLOCK);
}

void EventManager::registerEvent(Event *ev)
{
    struct epoll_event epoll_ev;
    memset(&epoll_ev, 0, sizeof epoll_ev);
    
    epoll_ev.events = ev->_events;
    epoll_ev.data.fd = ev->_fd;
    
    makeNonBlockingFD(ev->_fd);
    epoll_ctl(_epollHandle, EPOLL_CTL_ADD, ev->_fd, &epoll_ev);
    _events.insert(std::make_pair(ev->_fd, ev));
    ++ _eventCount;

    ev->onRegister(this);
}

EventManager::EventManager():
  _eventCount(0)
{
    _epollHandle = epoll_create1(0);
    if (_epollHandle < 0) {
        perror("epoll_create1");
        std::exit(EXIT_FAILURE);
    }
}

void EventManager::mainLoop()
{
    int n;
    struct epoll_event eevs[10];

    while (1) {
        n = epoll_wait(_epollHandle, eevs, 10, -1);
        for (int i = 0; i < n; ++i) {
            int fd = eevs[i].data.fd;
            auto itr = _events.find(fd);
            if (itr != _events.end()) {
                Event *ev = itr->second;
                if (!ev->eventHandler(eevs[i].events))
                    unregisterEvent(ev);
            }
        }
        cleanDeleteList();
        if (_eventCount == 0)
            break;
    }
    rz_debug("no more event: exiting main event loop\n");
}

void EventManager::cleanDeleteList()
{
    while (_deleteSet.size() > 0) {
        auto i = _deleteSet.begin();
        delete *i;
        _deleteSet.erase(i);
    }
}

EventManager::~EventManager()
{
    close(_epollHandle);
}
