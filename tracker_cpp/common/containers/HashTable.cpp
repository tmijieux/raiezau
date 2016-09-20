#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>

#include <pthread.h>

#include "uthash.h"
#include "HashTable.hpp"
#include "List.hpp"

using std::string;

struct h_entry {
    h_entry(string key, void *data):
        key(strdup(key.c_str())), data(data) {}
    ~h_entry() { delete key; }

    char *key;
    void *data;
    UT_hash_handle hh;
};

int HashTable::size() const
{
    return HASH_COUNT(m_head);
}

HashTable::HashTable(size_t /*size*/)
{
    pthread_rwlock_init(&m_rwlock, NULL);
}

bool HashTable::addEntry(string key, void *data)
{
    h_entry *entry = new h_entry(key, data);
    pthread_rwlock_wrlock(&m_rwlock);
    HASH_ADD_STR(m_head, key, entry);
    pthread_rwlock_unlock(&m_rwlock);
    return true;
}

bool HashTable::addUniqueEntry(string key, void *data)
{
    bool no_err = true;
    h_entry *entry;

    pthread_rwlock_wrlock(&m_rwlock);
    HASH_FIND_STR(m_head, key.c_str(), entry);
    if (entry == NULL) {
        entry = new h_entry(key, data);
        HASH_ADD_STR(m_head, key, entry);
        no_err = false;
    }
    pthread_rwlock_unlock(&m_rwlock);
    return no_err;
}

bool HashTable::removeEntry(string key)
{
    bool del = false;
    h_entry *entry;

    pthread_rwlock_wrlock(&m_rwlock);
    HASH_FIND_STR(m_head, key.c_str(), entry);
    if (entry != NULL) {
        HASH_DEL(m_head, entry);
        delete entry;
        del = true;
    }
    pthread_rwlock_unlock(&m_rwlock);

    return del;
}

bool HashTable::hasEntry(string key)
{
    bool res;
    h_entry *entry;

    pthread_rwlock_rdlock(&m_rwlock);
    HASH_FIND_STR(m_head, key.c_str(), entry);
    res = (entry != NULL);
    pthread_rwlock_unlock(&m_rwlock);
    return res;
}

bool HashTable::getEntry(string key, void *ret)
{
    bool err = false;
    h_entry *entry;

    pthread_rwlock_rdlock(&m_rwlock);
    HASH_FIND_STR(m_head, key.c_str(), entry);
    if (NULL != entry) {
        *((void**)ret) = entry->data;
        err = true;
    }
    pthread_rwlock_unlock(&m_rwlock);
    return err;
}

HashTable::~HashTable()
{
    h_entry *entry, *tmp;
    HASH_ITER(hh, m_head, entry, tmp) {
        HASH_DEL(m_head, entry);
        delete entry;
    }
    pthread_rwlock_destroy(&m_rwlock);
}

void HashTable::forEach(std::function<void(char*, void*)> func)
{
    h_entry *entry, *tmp;

    pthread_rwlock_rdlock(&m_rwlock);
    HASH_ITER(hh, m_head, entry, tmp) {
        func(entry->key, entry->data);
    }
    pthread_rwlock_unlock(&m_rwlock);
}

List *HashTable::toList()
{
    h_entry *entry, *tmp;
    List *l = new List();

    pthread_rwlock_rdlock(&m_rwlock);
    HASH_ITER(hh, m_head, entry, tmp) {
        l->add(entry->data);
    }
    pthread_rwlock_unlock(&m_rwlock);
    return l;
}
