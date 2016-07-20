#ifndef EVENT_H
#define EVENT_H

class Event;

#include <cstdint>
#include "./EventManager.hpp"

class Event {
    friend class EventManager;
    
public:
    Event(int fd, uint32_t events, bool autoDelete = false);
    virtual bool eventHandler(uint32_t events) = 0;

    void scheduleUnregister();
    void scheduleAutoDelete();
    virtual ~Event();

protected:
    virtual void onRegister(EventManager*) {}
    
private:
    int _fd;
    uint32_t _events;
    bool _unregister;
    bool _autoDelete;
};

#endif //EVENT_H
