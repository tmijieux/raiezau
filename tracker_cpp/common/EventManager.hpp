#ifndef EVENTMANAGER_H
#define EVENTMANAGER_H

class EventManager;

#include <string>
#include <unordered_map>
#include <set>
#include "./Event.hpp"

using std::string;

class EventManager {

public:
    EventManager();
    ~EventManager();

    void registerEvent(Event*);
    void rearmEvent(Event*);
    void unregisterEvent(Event*);
    void mainLoop();

    static void makeNonBlockingFD(int fd);
private:

    void cleanDeleteList();

    int _epollHandle;
    uint32_t _eventCount;
    std::unordered_map<int, Event*> _events;
    std::set<Event*> _deleteSet;
};

#endif //EVENTMANAGER_H
