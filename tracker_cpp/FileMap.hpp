#ifndef FILEMAP_H
#define FILEMAP_H

#include <string>
#include <utility>
#include <unordered_map>

class File;
class FileMap;

typedef std::unordered_map<std::string, File*> FileMapBase;

class FileMap : public FileMapBase {
public:
    FileMap() = default;
    File *get(std::string hash);
    std::pair<iterator,bool> insert(File*);
};

#endif //FILEMAP_H
