#ifndef BUFFER_H
#define BUFFER_H

#include <cstdint>
#include <string>

class Buffer {
public:
    virtual int write(uint8_t *data, size_t size) = 0;
    virtual int write(void *data, size_t size) = 0;
    virtual int writeToFd(int fd) = 0;
    virtual int read(uint8_t *data, size_t size) = 0;
    virtual int read(void *data, size_t size) = 0;
    virtual int peek(uint8_t *data, size_t size) = 0;
    virtual int peek(void *data, size_t size) = 0;
    virtual void resize(float factor) = 0;
    virtual size_t remainingSpace() const = 0;
    virtual size_t capacity() const = 0;
    virtual size_t size() const = 0;
    virtual void reset() = 0;

    virtual bool haveLine() const = 0;
    virtual bool haveWord() const = 0;
    virtual std::string readUntil(char c) = 0;

    template<typename T>
    Buffer& operator>>(T &v)
    {
        this->read(&v, sizeof v);
        return *this;
    }

    template<typename T>
    Buffer& operator<<(const T &v)
    {
        this->write((uint8_t*) &v, sizeof v);
        return *this;
    }

    virtual ~Buffer() {}
};

#endif //BUFFER_H
