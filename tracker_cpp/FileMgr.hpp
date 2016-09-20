#ifndef FILEMGR_H
#define FILEMGR_H

#include <string>
#include <utility>

class File;
class FileMgr;

#include "./FileMap.hpp"
#include "./File.hpp"

class FileMgr : public FileMap {
public:
    static FileMgr &instance();

    File *getFile(std::string fileName, size_t fileSize,
                         size_t pieceSize, std::string hash);
    const FileSet &getSet();
    std::pair<FileMap::iterator, bool> insert(File *);

private:
    FileMgr() = default;
    FileSet _fileSet;
};

#define sFileMgr (FileMgr::instance())

#endif //FILEMGR_H
