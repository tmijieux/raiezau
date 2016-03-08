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
	ServerThread.java \
	Server.java \
	Tracker.java \
	Strategy.java \
	StrategyTest.java \
	Client.java

KLASS=$(SRC:.java=.class)
CLASS=$(foreach klass, $(KLASS), RZ/$(klass))
CLASSFILES=$(foreach klass, $(CLASS), build/$(klass))

all:  src

src: build
	mkdir -p build/
	javac -classpath .:jar/jyaml-1.3.jar -d build/ $(SRC)

run:
	@java -cp build/:jyaml/ RZ.Client

build:
	mkdir -p build/

jar: rz_client.jar

rz_client.jar: $(CLASSFILES)
	rm -rf rz_client.jar
	jar cfm $@ Manifest.txt -C build .

run_jar: rz_client.jar
	@java -jar rz_client.jar

clean:
	$(RM) -r build/*
	$(RM) -r build-tst/*

nc:
	nc -l -p 8080

nc_track:
	nc -l -p 8080 < track.txt

nc_cli:
	nc -l -p 6969 < client.txt
