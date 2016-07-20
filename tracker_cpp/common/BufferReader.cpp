#include <cassert>
#include <iostream>
#include <cstring>
#include "./BufferReader.hpp"

BufferReader::BufferReader(uint8_t *data, size_t size):
    _data(data), _size(size), _r(0)
{
}

void BufferReader::read(uint8_t *data, size_t size)
{
    assert(_r + size <= _size);
    std::memcpy(data, _data + _r, size);
    _r += size;
}

void BufferReader::read(void *data, size_t size)
{
    read((uint8_t*) data, size);
}

size_t BufferReader::remainingSize() const
{
    return _size - _r;
}

BufferReader &BufferReader::operator>>(std::string &left)
{
    left.clear();
    while (_data[_r]) {
        left += _data[_r];
        ++_r;
    }
    ++_r;
    return *this;
}
