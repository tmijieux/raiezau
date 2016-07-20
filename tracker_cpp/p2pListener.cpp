#include "./common/error/error.hpp"
#include "./p2pListener.hpp"
#include "./Peer.hpp"

p2pListener::p2pListener(ThreadPool &tp, uint16_t port, EventManager &eventManager):
    ServerSocket("0.0.0.0", port),
    _threadPool(tp),
    _eventManager(eventManager)
{
    
}

bool p2pListener::acceptHandler()
{
    while (_readBuf->size() > 0) {
        int accepted;
        
        if (_readBuf->size() < sizeof accepted) {
            // unexpected error
            rz_debug("unexpected listener error\n");
            rz_debug("suspending listener ...\n");
            return false;
        }
            
        *_readBuf >> accepted;

        Peer *peer = new Peer(accepted, _eventManager);
        peer->scheduleAutoDelete();
        _eventManager.registerEvent(peer);
    }

    return true;
}
