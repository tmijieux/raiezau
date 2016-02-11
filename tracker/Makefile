TARGET=trackme
SRC=$(wildcard *.c)
CFLAGS=-Wall -std=gnu99 -I..
ifdef DEBUG
	CFLAGS+=-ggdb -O0 -DDEBUG
else
	CFLAGS+=-g -O2 -march=native
endif

LDFLAGS= -L. -lgc -lyaml -lcrypto -lpthread -lrz_cutil -lreadline

OBJ=$(SRC:.c=.o) librz_cutil.a
DEP=$(SRC:.c=.d) 

all: $(TARGET)

-include $(DEP)

$(TARGET): $(OBJ)
	$(CC) $^ -o $@ $(LDFLAGS)

%.o: %.c
	gcc -c $(CFLAGS) $*.c -o $*.o
	gcc -MM $(CFLAGS) $*.c > $*.d

librz_cutil.a:
	make -C ../cutil
	$(AR) cr $@ ../cutil/*.o

clean: 
	$(RM) $(OBJ) $(DEP)
	make -C ../cutil clean
