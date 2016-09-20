#ifndef BUFFERWRITER_H
#define BUFFERWRITER_H

#include <cstdint>
#include <string>

class BufferWriter {
public:
    BufferWriter(uint8_t *data, size_t size);

    void write(uint8_t *data, size_t size);
    void write(void *data, size_t size);

    BufferWriter &operator<<(const std::string &left)
    {
        write((uint8_t*) left.c_str(), left.length()+1);
        return *this;
    }

    template<typename T>
    BufferWriter &operator<<(const T &left)
    {
        static_assert(std::is_class<T>::value == false,
                      "cant apply this operator on a class");
        write((uint8_t*) &left, sizeof left);
        return *this;
    }
    off_t offset() const;

private:
    uint8_t *_data;
    size_t _size;
    off_t _w;
};

#endif //BUFFERWRITER_H
