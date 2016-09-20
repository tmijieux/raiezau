TARGET=rz_peer
SRC=$(wildcard *.c)
CFLAGS+=-Wall -std=gnu99 $(shell pkg-config --cflags glib-2.0 gio-2.0) \
	-I/usr/include/glib-2.0 -I/usr/lib/glib-2.0/include

DEBUG=1
ifdef DEBUG
	CFLAGS+=-ggdb -O0 -Wconversion -DDEBUG -fsanitize=address -fsanitize=undefined
else
	CFLAGS+=-g -O2 -march=native
endif

LDFLAGS= -L. $(shell pkg-config --libs glib-2.0 gio-2.0) -L/usr/lib -lm -lglib-2.0
ifdef DEBUG
	LDFLAGS+=-fsanitize=address -fsanitize=undefined
endif

OBJ=$(SRC:.c=.o)
DEP=$(SRC:.c=.d)

all: $(TARGET)

-include $(DEP)

$(TARGET): $(OBJ)
	$(CC) $^ -o $@ $(LDFLAGS)

%.o: %.c
	@gcc -MM $(CFLAGS) $*.c > $*.d
	gcc -c $(CFLAGS) $*.c -o $*.o

clean:
	$(RM) $(OBJ) $(DEP) *.d *.o
	make -C ../cutil clean

mrproper: clean
	$(RM) $(TARGET)

