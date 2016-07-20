#include "./FileMgr.hpp"
#include "./File.hpp"

using namespace std;

FileMgr &FileMgr::instance()
{
    static FileMgr instance;
    return instance;
}

File *FileMgr::getFile(string fileName, size_t fileSize,
                       size_t pieceSize, string hash)
{
    File *f = new File(fileName, fileSize, pieceSize, hash);
    auto it = sFileMgr.find(hash);
    if (it != sFileMgr.end() && *it->second == *f) {
        delete f;
        f = it->second;
    } else
        sFileMgr.insert(f);
    
    return f;
}

std::pair<FileMap::iterator, bool>
FileMgr::insert(File *f)
{
    _fileSet.insert(f);
    return FileMap::insert(f);
}


const FileSet &FileMgr::getSet()
{
    return _fileSet;
}
