#ifndef P2PLISTENER_H
#define P2PLISTENER_H

#include "./common/network/ServerSocket.hpp"

class ThreadPool;

class p2pListener : public ServerSocket {
public:
    p2pListener(ThreadPool &tp, uint16_t port, EventManager &EM);

protected:
    bool acceptHandler() override final;

private:
    ThreadPool &_threadPool;
    EventManager &_eventManager;
};

#endif //P2PLISTENER_H
