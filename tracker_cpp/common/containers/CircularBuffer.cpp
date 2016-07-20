#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <unistd.h>
#include <iostream>

#include "./CircularBuffer.hpp"
#include "../error/error.hpp"
#define min(x, y)   ((x) < (y) ? (x) : (y))
using namespace std;

CircularBuffer::CircularBuffer(size_t capacity):
    _capacity(capacity), _size(0), _r(0), _w(0),
    _buf(new uint8_t[capacity])
{
}

CircularBuffer::~CircularBuffer()
{
    delete [] _buf;
}

int CircularBuffer::write(uint8_t *data, size_t size)
{
    if (size <= remainingSpace()) {
        if (_w + size <= _capacity) {
            memcpy(_buf + _w, data, size);
            _w += size;
        } else {
            size_t cut = _capacity - _w;
            memcpy(_buf + _w, data, cut);
            memcpy(_buf, data+cut, size - cut);
            _w = size - cut;
        }
        _size += size;
        return size;
    } else {
        abort();
    }
    return -1;  
}

int CircularBuffer::write(void *data, size_t size)
{
    return write((uint8_t*) data, size);
}

int CircularBuffer::read(uint8_t *data, size_t size)
{
    if (size <= _size) {
        if (_r + size <= _capacity) {
            memcpy(data, _buf + _r, size);
            _r += size;
        } else {
            size_t cut = _capacity - _r;
            memcpy(data, _buf + _r, cut);
            memcpy(data+cut, _buf, size - cut);
            _r = size - cut;
        }
        _size -= size;
        return size;
    } else {
        abort();
    }
    return -1;    
}

int CircularBuffer::read(void *data, size_t size)
{
    return read((uint8_t*) data, size);
}

int CircularBuffer::peek(uint8_t *data, size_t size)
{
    if (size <= _size) {
        if (_r + size < _capacity) {
            memcpy(data, _buf + _r, size);
        } else {
            size_t cut = _capacity - _r;
            memcpy(data, _buf + _r, cut);
            memcpy(data+cut, _buf, size - cut);
        }
        return size;
    }
    return peek(data, _size);
}

int CircularBuffer::peek(void *data, size_t size)
{
    return peek((uint8_t*) data, size);
}

size_t CircularBuffer::size() const
{
    return _size;
}

size_t CircularBuffer::remainingSpace() const
{
    return _capacity - _size;
}

void CircularBuffer::resize(float factor)
{
    if (factor <= 1)
        return;
    
    size_t size = _size;
    size_t new_cap = _capacity * factor;
    uint8_t *nbuf = new uint8_t[new_cap];

    this->read(nbuf, min(size, new_cap));
    _r = 0;
    _w = size;
    
    delete [] _buf;
    _buf = nbuf;
    _size = size;
    _capacity = new_cap;
    rz_debug("resize @%p: *%f: new capacity = %zu\n", this,
             factor, new_cap);
}

size_t CircularBuffer::capacity() const
{
    return _capacity;
}

int CircularBuffer::writeToFd(int fd)
{
    int ret;
    if (_r + _size <= _capacity) {
        if ((ret = ::write(fd, _buf + _r, _size)) <= 0)
            return ret;
        else if ((size_t)ret < _size) {
            _r += ret;
            _size -= ret;
            return ret;
        }
    } else {
        size_t cut = _capacity - _r;
        if ((ret = ::write(fd, _buf + _r, cut)) <= 0)
            return ret;
        else if ((size_t) ret < cut) {
            _r += ret;
            _size -= ret;
            return ret;
        }
        if ((ret = ::write(fd, _buf, _size - cut)) <= 0)
            return ret;
        else if ((size_t) ret < _size - cut) {
            _r = ret;
            _size -= ret;
            return ret;
        }
    }
    size_t oldSize = _size;
    _size = 0;
    _r = _w = 0;
    return oldSize;
}

void CircularBuffer::reset()
{
    _r = _w = 0;
    _size = 0;
}

bool CircularBuffer::contains(char c) const
{
    size_t i;
    
    if (_r < _w) {
        for (i= _r; i < _w; ++i)
            if (_buf[i] == (uint8_t) c)
                return true;
    } else if (_r > _w) {
        for (i= _r; i < _capacity; ++i)
            if (_buf[i] == (uint8_t) c)
                return true;
        for (i = 0; i < _w; ++i)
            if (_buf[i] == (uint8_t) c)
                return true;
    }
    return false;
}

bool CircularBuffer::haveWord() const
{
    return contains(' ');
}

bool CircularBuffer::haveLine() const
{
    return contains('\n');
}

string CircularBuffer::readUntil(char c)
{
    string s;
    s.clear();
    
    if (_r > _w) {
        while (_r < _capacity && _buf[_r] != (uint8_t) c) {
            s += (char) _buf[_r++];
            -- _size;
        }
        _r = 0;
    }
    while (_r < _w && _buf[_r] != (uint8_t) c) {
        s += (char) _buf[_r++];
        -- _size;
    }
    if (_size > 0) {
        ++_r; -- _size;
    }
    return s;
}

