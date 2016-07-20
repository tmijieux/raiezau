
#include "./FileMap.hpp"
#include "./File.hpp"

using namespace std;

File *FileMap::get(string hash)
{
    auto it = find(hash);
    if (it != end())
        return it->second;
    return NULL;
}

pair<FileMap::iterator,bool> FileMap::insert(File *f)
{
    return FileMapBase::insert(make_pair(f->_hash, f));
}
