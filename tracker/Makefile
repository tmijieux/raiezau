TARGET=trackme
SRC=$(wildcard *.c) $(wildcard ../cutil/*.c)
CFLAGS+=-Wall -std=gnu99 -I..
DEBUG=1
ifdef DEBUG
	CFLAGS+=-ggdb -O0 -DDEBUG
else
	CFLAGS+=-g -O2 -march=native
endif

LDFLAGS= -L. -lyaml -lcrypto -lpthread -lreadline

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

