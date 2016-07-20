#ifndef STACK_H
#define STACK_H

template<typename T> class Stack;

#include <stdexcept>
#include <stdlib.h>
#include <vector>

template <typename T>
class Stack {
public:
    class EmptyStackException : public std::range_error {
        EmptyStackException() = default;
        EmptyStackException(std::string str): std::range_error(str) {};
    };
    
    Stack(size_t bufSize);
    virtual ~Stack();

    void push(T element);
    T& peek();
    T pop();

    bool isEmpty();
    size_t size();
    
private:
    int m_head;
    std::vector<T> m_buf;
};

template<typename T>
Stack<T>::Stack(size_t bufSize):
    m_buf(bufSize)
{
}

template<typename T>
Stack<T>::~Stack()
{
}

template<typename T>
T& Stack<T>::peek()
{
    return m_buf[m_head];
}

template<typename T>
T Stack<T>::pop()
{
    if (isEmpty())
        throw EmptyStackException("cannot pop an empty stack");
    return m_buf[m_head--];
}

template<typename T>
void Stack<T>::push(T elem)
{
    m_buf[++m_head] = elem;
}

template<typename T>
bool Stack<T>::isEmpty()
{
    return m_head == 0;
}

template<typename T>
size_t Stack<T>::size()
{
    return m_head;
}
    
#endif //STACK_H
