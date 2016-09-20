#ifndef PEER_H
#define PEER_H

#include <cstdint>
#include <unordered_map>
#include <string>

class Peer;

#include "./common/EventManager.hpp"
#include "./common/network/ClientSocket.hpp"
#include "./File.hpp"
#include "./FileMap.hpp"

class Peer : public ClientSocket {
public:
    Peer(int sock, EventManager &eventManager);
    std::string to_string() const override;


protected:
    virtual bool readHandler() override final;

private:
    void error();

    void handleAnnounce(std::stringstream&);
    void handleUpdate(std::stringstream&);
    void handleLook(std::stringstream&);
    void handleGetfile(std::stringstream&);

    void sendErrorNotice(std::string notice);
    void sendOK();

    void parseAnnounceSeed(std::string seed);
    void parseAnnounceLeech(std::string leech);
    void parseLook(std::string look);
    FileSet parseCriterion(string criterion, const FileSet&);

    std::string peerListString(File *f);

    uint16_t _listeningPort;
    bool _announced;

    FileMap _leech;
    FileMap _seed;

    uint32_t _seqErrorCount;

    typedef void (Peer::*PeerHandler)(std::stringstream&);
    typedef std::unordered_map<std::string, PeerHandler> PeerHandlerMap;

    static PeerHandlerMap initHandlers();
    static PeerHandlerMap sHandlerMap;

    static const size_t MAX_KEYWORD_SIZE = 8;
    static const uint32_t MAX_SEQ_ERROR_COUNT = 5;
};

#endif //PEER_H


