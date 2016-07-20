#include <string>
#include <sstream>
#include <limits>
#include <vector>
#include <iostream>
#include <algorithm>
#include <arpa/inet.h>

#include "./Peer.hpp"
#include "./FileMgr.hpp"
#include "./File.hpp"
#include "./common/error/error.hpp"
#include "./Criterion.hpp"
#include "./common/Util.hpp"

using namespace std;

  
class rz_exception : public exception {
public:
    rz_exception(const std::string s): _s(s) {}
    const char *what() const noexcept override { return (_s).c_str(); }
private:
    string _s;
};

Peer::PeerHandlerMap Peer::initHandlers()
{
    PeerHandlerMap PHmap;
    PHmap["announce"] = &Peer::handleAnnounce;
    PHmap["update"] = &Peer::handleUpdate;
    PHmap["look"] = &Peer::handleLook;
    PHmap["getfile"] = &Peer::handleGetfile;
    return PHmap;
}

Peer::PeerHandlerMap Peer::sHandlerMap = initHandlers();



Peer::Peer(int sock, EventManager &eventManager):
    ClientSocket(sock, eventManager),
    _listeningPort(0), _announced(false),
    _seqErrorCount(0)
{
}

void Peer::parseAnnounceSeed(string seed)
{
    _seed.clear();

    vector<string> v;
    v = split(seed, ' ');
    v.erase(
        std::remove_if(v.begin(), v.end(), [](const string&s){return s == "";}),
        v.end()
    );

    if (v.size() % 4 != 0)
        throw rz_exception("invalid seed argument count");

    for (auto it = v.begin(); it != v.end(); it+=4) {
        try {
            File *f;
            f = sFileMgr.getFile(*it, stoul(*(it+1)), stoul(*(it+2)), *(it+3));
            _seed.insert(f);
            f->addPeer(this);
        } catch (const exception &e) {
            cerr << "invalid file: " << e.what() << endl;
        }
    }
}

void Peer::parseAnnounceLeech(string leech)
{
    _leech.clear();
    
    vector<string> v;
    v = split(leech, ' ');
    v.erase(
        std::remove_if(v.begin(), v.end(), [](const string&s){return s == "";}),
        v.end());

    for (auto it = v.begin(); it != v.end(); ++it) {
        File *f = sFileMgr.get(*it);
        if (f != NULL) {
            _leech.insert(f);
            f->addPeer(this);
        }
    }
}

void Peer::handleAnnounce(stringstream &line)
{
    bool listen, seed, leech;
    listen = seed = leech = false;
    
    string keyword, argument, garbage;

    if (_announced)
        throw rz_exception("already announced");

    int k = 0;
    while (k != 3) {
        line >> keyword;
        if (keyword == "listen") {
            line >> argument;
            try {
                int port = stoi(argument);
                if (port <= 0 || port > numeric_limits<uint16_t>::max())
                    throw rz_exception("listen port is zero or too big");
                _listeningPort = (uint16_t) port;
            } catch (const exception &e) {
                throw rz_exception("invalid listening port");
            }
            listen = true;
        } else if (keyword == "seed") {
            getline(line, garbage, '[');
            getline(line, argument, ']');
            parseAnnounceSeed(argument);
            seed = true;
        } else if (keyword == "leech") {
            getline(line, garbage, '[');
            getline(line, argument, ']');
            parseAnnounceLeech(argument);
            leech = true;
        } else
            throw rz_exception("unexpected token '"+keyword+"'");
        ++ k;
    }
    if (!listen || !seed || !leech)
        throw rz_exception("invalid request");
    
    _announced = true;
    sendOK();
}

void Peer::handleUpdate(stringstream &line)
{
    if (!_announced)
        throw rz_exception("not announced.");
    string keyword, argument, garbage;
    bool seed(false), leech(false);

    int k = 0;
    while (k != 2) {
        line >> keyword;
        if (keyword == "seed") {
            getline(line, garbage, '[');
            getline(line, argument, ']');
            parseAnnounceLeech(argument);
            seed = true;
        } else if (keyword == "leech") {
            getline(line, garbage, '[');
            getline(line, argument, ']');
            parseAnnounceLeech(argument);
            leech = true;
        } else
            throw rz_exception("unexpected token '"+keyword+"'");
        ++ k;
    }
    if (!seed || !leech)
        throw rz_exception("invalid request");
    sendOK();
}

void operator&=(FileSet &lhs, const FileSet &rhs)
{
    for (auto it : lhs) {
        auto jt = rhs.find((File*) it);
        if (jt == rhs.end())
            lhs.erase(it);
    }
}

FileSet Peer::parseCriterion(string criterion, const FileSet &s)
{
    const CriterionVec &c = CriterionVec::criterions;
    for (auto it = c.begin(); it != c.end(); ++it) {
        if (it->get()->match(criterion))
            return it->get()->applyCriterion(criterion, s);
    }
    return FileSet();
}

string fileListString(FileSet s)
{
    stringstream ss;
    ss << "list [";
    for (auto it = s.begin(); it != s.end(); ++it) {
        if (it != s.begin())
            ss << " ";
        ss << (*it)->to_string();
    }
    ss << "]\n";
    return ss.str();
}

void Peer::parseLook(string look)
{
    vector<string> v = split(look, ' ');
    
    FileSet s = sFileMgr.getSet();
    for (auto it = v.begin(); it != v.end(); ++it)
        s = parseCriterion(*it, s);

    write(fileListString(s));
}

void Peer::handleLook(stringstream& line)
{
    if (!_announced)
        throw rz_exception("not announced.");
    
    string garbage, argument;
    getline(line, garbage, '[');
    getline(line, argument, ']');
    parseLook(argument);
}

std::string Peer::peerListString(File *f)
{
    stringstream ss;
    ss << "peers " << f->hash() << " [";
    const File::PeerSet &m = f->getPeers();
    int k = 0;
    for (auto it = m.begin(); it != m.end(); ++it) {
        if ((*it) == this)
            continue;
        if (k != 0)
            ss << " ";
        ss << (*it)->to_string();
        ++ k;
    }
    ss << "]\n";
    return ss.str();
}

std::string Peer::to_string() const
{
    stringstream ss;
    char addr[20] = {0};
    uint32_t ip = getRemoteIP();
    
    inet_ntop(AF_INET, &ip, addr, 19);
    ss << addr << ":" << _listeningPort;
    return ss.str();
}

void Peer::handleGetfile(stringstream &line)
{
    if (!_announced)
        throw rz_exception("not announced.");
    string key;
    line >> key;
    File *f = sFileMgr.get(key);
    if (f != NULL)
        write(peerListString(f));
    else {
        stringstream ss;
        ss << "peers " << f->hash() <<" []\n";
        write(ss.str());
    }
}

void Peer::sendOK()
{
    write("ok\n");
}

void Peer::sendErrorNotice(string notice)
{
    string s = "error: ";
    s += notice; s += '\n';
    write((uint8_t*) s.c_str(), (size_t)s.length());
}

bool Peer::readHandler()
{
    while (_readBuf->size() > 0) {
        if (!_readBuf->haveLine()) {
            if (!_readBuf->haveWord() && _readBuf->size() > MAX_KEYWORD_SIZE)
                return false;
            return true;
        }

        string line = _readBuf->readUntil('\n');
        stringstream ss(line);
        string keyword;
        ss >> keyword;
        if (keyword == "")
            continue; // allow empty lines
        
        auto it = sHandlerMap.find(keyword);
        if (it == sHandlerMap.end()) {
            sendErrorNotice(string("unexpected keyword '") + keyword + "'");
            ++ _seqErrorCount;
            if (_seqErrorCount > MAX_SEQ_ERROR_COUNT)
                return false;
            continue;
        }
        
        PeerHandler handler = it->second;
        try {
            (this->*handler)(ss);
        } catch (const exception &e) {
            sendErrorNotice(e.what());
            rz_error("%s\n", e.what());
            ++ _seqErrorCount;
            if (_seqErrorCount > MAX_SEQ_ERROR_COUNT)
                return false;
            continue;
        }
        _seqErrorCount = 0;
    }
    return true;
}

void Peer::error()
{
    ++ _seqErrorCount;
    if (_seqErrorCount > MAX_SEQ_ERROR_COUNT)
        close();
}
