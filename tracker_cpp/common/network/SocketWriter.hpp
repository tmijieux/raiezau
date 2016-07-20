#ifndef SOCKETWRITER_H
#define SOCKETWRITER_H

#include "../Event.hpp"
#include "../containers/Buffer.hpp"

class SocketWriter : public Event {
public:
    SocketWriter(int sock, Buffer *b, EventManager &EM);
    ~SocketWriter();
    bool eventHandler(uint32_t events) override;
    void rearm();
    
    static int makeSockFD(int sock);

    
private:
    int _sfd;
    Buffer *_buf;
    bool _armed;
    EventManager &_eventManager;
};

#endif //SOCKETWRITER_H
