.PHONY = clean build recette build-tst run-tst run

SRC=Config.java \
	Logs.java \
	RZFile.java \
	RZPattern.java \
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
	javac -classpath .:jar/jyaml-1.3.jar -d build/ $(SRC)

run: src
	java -cp build/:jyaml/ RZ.Client 

clean:
	$(RM) -r build/*
	$(RM) -r build-tst/*

nc:
	nc -l -p 8080

nc_track:
	nc -l -p 8080 < track.txt

nc_cli:
	nc -l -p 6969 < client.txt
