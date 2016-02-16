.PHONY = clean build recette build-tst run-tst run

SRC=File.java \
	CriterionField.java \
	CriterionOP.java \
	Criterion.java \
	LookRequest.java \
	PeerSocket.java \
	Peer.java \
	Tracker.java \
	Client.java

re:
	make -B src

src:
	mkdir -p build/
	javac -d build/ $(SRC)

run: src
	java -cp build/ RZ.Client 

clean:
	$(RM) -r build/*
	$(RM) -r build-tst/*

nc:
	nc -l -p 8080
