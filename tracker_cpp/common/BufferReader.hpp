#ifndef BUFFERREADER_H
#define BUFFERREADER_H

#include <cstdint>
#include <string>
#include <iostream>

class BufferReader {
public:
    BufferReader(uint8_t *data, size_t size);

    void read(uint8_t *data, size_t size);
    void read(void *data, size_t size);


    BufferReader &operator>>(std::string &left);

    template<typename T>
    BufferReader &operator>>(T &left)
    {
        static_assert(std::is_class<T>::value == false);
        read((uint8_t*) &left, sizeof left);
        return *this;
    }

    size_t remainingSize() const;

private:
    uint8_t *_data;
    size_t _size;
    off_t _r;
};

#endif //BUFFERREADER_H
