.PHONY = clean build recette build-tst run-tst run

SRC=Client.java

re:
	make -B src

src:
	mkdir -p build/
	javac -d build/ $(SRC)

run: src
	java -cp build Client 

clean:
	$(RM) -r build/*
	$(RM) -r build-tst/*

nc:
	nc -l -p 8080
