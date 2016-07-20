CC=g++
TARGET=trackmepp
SRC=$(wildcard *.cpp) \
    $(wildcard ./common/*.cpp) \
    $(wildcard ./common/error/*.cpp) \
    $(wildcard ./common/containers/*.cpp) \
    $(wildcard ./common/network/*.cpp) \
    $(wildcard ./common/crypto/SHA1.cpp) \
    $(wildcard ./common/crypto/BigNumber.cpp) \

CFLAGS+=-Wall -Wextra -std=c++14 -I.. -DPURIFY -fsanitize=undefined -fsanitize=address
DEBUG=1

ifdef DEBUG
	CFLAGS+=-ggdb -O0 -DDEBUG
else
	CFLAGS+=-g -O2 -march=native
endif

LDFLAGS= -L. -lpthread -lcrypto  -fsanitize=undefined -fsanitize=address

OBJ=$(SRC:.cpp=.o) 
DEP=$(SRC:.cpp=.d) 

all: $(TARGET)

-include $(DEP)

$(TARGET): $(OBJ)
	$(CC) $^ -o $@ $(LDFLAGS)

%.o: %.cpp
	@$(CC) -MM $(CFLAGS) $*.cpp > $*.d
	$(CC) -c $(CFLAGS) $*.cpp -o $*.o

clean: 
	$(RM) $(OBJ) $(DEP) *.d *.o

mrproper: clean
	$(RM) $(TARGET)

