#include <cassert>
#include <iostream>
#include <cstring>
#include "./BufferWriter.hpp"

BufferWriter::BufferWriter(uint8_t *data, size_t size):
    _data(data), _size(size), _w(0)
{
}

void BufferWriter::write(uint8_t *data, size_t size)
{
    assert(_w + size <= _size);
    std::memcpy(_data + _w, data, size);
    _w += size;
}

void BufferWriter::write(void *data, size_t size)
{
    write((uint8_t*) data, size);
}

off_t BufferWriter::offset() const
{
    return _w;
}
