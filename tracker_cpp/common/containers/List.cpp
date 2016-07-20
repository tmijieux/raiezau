
#include <pthread.h>
#include <stdarg.h>
#include "List.hpp"

class ListNode {
public:
    ListNode(void *data, bool isSentinel = false):
        m_data(data), m_isSentinel(isSentinel) {}
    ListNode *getNext() { return m_next; }
    void setNext(ListNode *next) { m_next = next; }
    void *getData() { return m_data; }
    void setData(void *data) { m_data = data; }
    bool isSentinel() { return m_isSentinel; }
    
private:
    void *m_data;
    ListNode *m_next;
    bool m_isSentinel;
};

ListNode *List::getNode(unsigned n)
{
    unsigned int k = n;
    pthread_mutex_lock(&m_mutex);
    ListNode *node = m_frontSentinel;
    if (m_curpos <= k) {
	k -= m_curpos;
	node = m_cursor;
    }
    for (unsigned int i = 0; i < k; i++)
	node = node->getNext();
    m_cursor = node;
    m_curpos = n;

    pthread_mutex_unlock(&m_mutex);
    return node;
}

size_t List::size() const
{
    return m_size;
}

List::List()
{
    pthread_mutex_t m = PTHREAD_MUTEX_INITIALIZER;
    m_mutex = m;
    
    pthread_mutex_lock(&m_mutex);
    m_frontSentinel = new ListNode(NULL, true);
    m_frontSentinel->setNext(new ListNode(NULL, true));
    m_cursor = m_frontSentinel;
    m_curpos = 0;
    m_size = 0;
    pthread_mutex_unlock(&m_mutex);
}

List::~List()
{
    ListNode *node = m_frontSentinel->getNext();
    ListNode *tmp = NULL;
    
    while (!node->isSentinel()) {
	tmp = node->getNext();
        delete node;
	node = tmp;
    }
    delete node;
    delete m_frontSentinel;
}

void *List::get(unsigned int n)
{
    return getNode(n)->getData();
}

void List::add(void *element)
{
    ListNode *tmp = new ListNode(element);
    pthread_mutex_lock(&m_mutex);
    tmp->setNext(m_frontSentinel->getNext());
    m_frontSentinel->setNext(tmp);
    m_size ++;
    if (m_curpos != 0)
        m_curpos++;
    pthread_mutex_unlock(&m_mutex);
}

void List::append(void *element)
{
    int n = size() + 1;
    insert(n, element);
}

void List::appendList(List *l2)
{
    for (unsigned i = 1; i <= l2->size(); ++i) {
	this->append(l2->get(i));
    }
}

void List::removeValue(void *value)
{
    ListNode *n, *next;
    pthread_mutex_lock(&m_mutex);
    m_curpos = 0;
    m_cursor = m_frontSentinel;
    n = m_frontSentinel;
    do {
        next = n->getNext();
        if (!next->isSentinel() && next->getData() == value) {
            m_size--;
            n->setNext( next->getNext());
            delete next;
        }
        n = n->getNext();
    } while (!n->isSentinel());
    pthread_mutex_unlock(&m_mutex);
}

void List::insert(unsigned n, void *element)
{
    ListNode *previous = getNode(n-1);
    ListNode *tmp = new ListNode(element);

    tmp->setNext(previous->getNext());
    previous->setNext(tmp);
    m_size ++;
}

void List::remove(unsigned n)
{
    ListNode *previous = getNode(n-1);
    ListNode *tmp = previous->getNext();
    previous->setNext(tmp->getNext());
    delete tmp;
    m_size --;
}

List *List::copy()
{
    List *n = new List;
    n->appendList(this);
    return n;
}

void **List::toArray()
{
    void **array = new void*[ m_size ];
    for (unsigned i = 0; i < m_size; ++i)
	array[i] = get(i);
    return array;
}

HashTable *List::toHashTable(std::function<const char*(void*)> keyname_func)
{
    HashTable *ht = new HashTable(2 * m_size);
    for (unsigned i = 0; i < m_size; ++i) {
	void *e = get(i);
	ht->addEntry(keyname_func(e), e);
    }
    return ht;
}

List *List::map(std::function<void*(void*)> func)
{
    int si = size();
    List *ret = new List();
    for (int i = 1; i <= si; ++i)
	ret->append(func(get(i)));
    return ret;
}

void List::forEach(std::function<void(void*)> func)
{
    int si = size();
    for (int i = 1; i <= si; ++i)
	func(get(i));
}
