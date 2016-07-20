#ifndef FILE_H
#define FILE_H

#include <cstdlib>
#include <cstdint>
#include <string>
#include <set>

class File;
class Peer;

typedef std::set<File*> FileSet;

class File {
    friend class FileMap;
    friend class FileMgr;
    
public:
    typedef std::set<Peer*> PeerSet;
    
    bool operator==(const File&);
    void addPeer(Peer *);
    const PeerSet &getPeers();
    std::string to_string();
    
    size_t size() const;
    size_t pieceSize() const;
    std::string hash() const;
    std::string name() const;
    
private:
    File(std::string fileName, size_t fileSize,
         size_t pieceSize, std::string hash);

    std::string _fileName;
    size_t _fileSize;
    size_t _pieceSize;
    std::string _hash;

    PeerSet _peers;
};

#endif //FILE_H
