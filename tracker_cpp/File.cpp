#include <string>
#include <sstream>

#include "./File.hpp"

using namespace std;

File::File(std::string fileName, size_t fileSize,
           size_t pieceSize, std::string hash):
    _fileName(fileName), _fileSize(fileSize),
    _pieceSize(pieceSize), _hash(hash)
{
}

bool File::operator==(const File &f)
{
    return _fileSize == f._fileSize && _fileSize == f._fileSize &&
        _pieceSize == f._pieceSize && _hash == f._hash;
}

void File::addPeer(Peer *p)
{
    _peers.insert(p);
}

const File::PeerSet& File::getPeers()
{
    return _peers;
}

std::string File::to_string()
{
    std::stringstream ss;
    ss << _fileName << " " << _fileSize << " " << _pieceSize << " " <<_hash;
    return ss.str();
}

size_t File::size() const
{
    return _fileSize;
}

size_t File::pieceSize() const
{
    return _pieceSize;
}

string File::hash() const
{
    return _hash;
}

string File::name() const
{
    return _fileName;
}
