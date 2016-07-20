#ifndef CIRCULARBUFFER_H
#define CIRCULARBUFFER_H

class CircularBuffer;

#include "./Buffer.hpp"

class CircularBuffer : public Buffer {
public:
    CircularBuffer(size_t capacity);
    virtual ~CircularBuffer();
    
    virtual int write(uint8_t *data, size_t size) override;
    virtual int write(void *data, size_t size) override;
    
    virtual int writeToFd(int fd) override;
    virtual int read(uint8_t *data, size_t size) override;
    virtual int read(void *data, size_t size) override;
    
    virtual int peek(uint8_t *data, size_t size) override;
    virtual int peek(void *data, size_t size) override;

    virtual void resize(float factor) override;
    virtual size_t remainingSpace() const override;
    virtual size_t capacity() const override;
    virtual size_t size() const override;
    virtual void reset() override;
    virtual bool haveLine() const override;
    virtual bool haveWord() const override;
    virtual std::string readUntil(char c) override;

private:

    bool contains(char c) const;
    size_t _capacity;
    size_t _size;
    size_t _r, _w;
    uint8_t *_buf;
};

#endif //CIRCULARBUFFER_H
