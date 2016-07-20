#ifndef GENERIC_H
#define GENERIC_H

#define SWAP(x, y, tmp)                         \
    do {                                        \
        tmp = x;                                \
        x = y;                                  \
        y = tmp;                                \
    } while (0)


static inline void *INT_PTR(long integer)
{
    return (void *) integer;
}

static inline int PTR_INT(void *pointer)
{
    return (long) pointer;
}




#endif //GENERIC_H
