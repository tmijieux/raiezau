#ifndef HASH_TABLE_H
#define HASH_TABLE_H

class HashTableImpl;
class HashTable;

#include <stdlib.h>
#include <string>
#include <functional>

#include "./List.hpp"

class HashTable {
    typedef std::string string;

public:
    HashTable(size_t size);
    virtual ~HashTable();

    bool addEntry(string key, void *data);
    bool addUniqueEntry(string key, void *data);
    bool removeEntry(string key);
    bool hasEntry(string key);
    bool getEntry(string key, void *ret);
    void forEach(std::function<void(char*key, void*data)> func);
    int size() const;
    List *toList();

private:
    struct h_entry *m_head;
    pthread_rwlock_t m_rwlock;
};

#endif //HASH_TABLE_H
