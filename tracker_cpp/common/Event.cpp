#include "./Event.hpp"

Event::Event(int fd, uint32_t events, bool autoDelete):
    _fd(fd), _events(events),
    _unregister(false), _autoDelete(autoDelete)
{
}

Event::~Event()
{
}

void Event::scheduleAutoDelete()
{
    _autoDelete = true;
}

void Event::scheduleUnregister()
{
    _unregister = true;
}
