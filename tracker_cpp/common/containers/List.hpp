#ifndef LIST_H
#define LIST_H

class List;

#include <stdlib.h>
#include "HashTable.hpp"

class ListNode;

class List {
    friend class ListImpl;
public:
    enum flags { ELEM, FREE };

    List();
    virtual ~List();

    size_t size() const;
    void *get(unsigned i);
    void add(void *data);
    void insert(unsigned i, void *data);
    void remove(unsigned i);
    void removeValue(void *data);
    void append(void *element);
    void appendList(List *l);
    List *copy();
    void **toArray();
    HashTable *toHashTable(std::function<const char*(void*)> keyname_func);
    void forEach(std::function<void(void*)> func);
    List *map(std::function<void*(void*)> func);

private:
    ListNode *getNode(unsigned n);

    ListNode *m_frontSentinel;
    ListNode *m_last;

    void (*free_element)(void*);
    ListNode *m_cursor;
    size_t m_size;
    unsigned m_curpos;
    pthread_mutex_t m_mutex;
};

#endif //LIST_H
