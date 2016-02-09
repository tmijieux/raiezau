.PHONY = clean build recette build-tst run-tst run

SRC=Client.java

re:
	make -B src

src:
	javac -d build/ $(SRC)

run: src
	java -cp build Client 

clean:
	rm -rf build/*
	rm -rf build-tst/*

nc:
	nc -l 8080
