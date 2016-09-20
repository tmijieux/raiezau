#include <cstdlib>
#include <cstdint>

#include "./common/EventManager.hpp"
#include "./p2pListener.hpp"

class Config;
class ThreadPool {};

int main(int /*argc*/, char */*$argv*/[])
{

    uint16_t port = 8080;//Config::getValue<uint16_t>("listeningPort");

    EventManager EM;
    ThreadPool tp;
    p2pListener s(tp, port, EM);

    EM.registerEvent(&s);
    EM.mainLoop();

    return EXIT_SUCCESS;
}
