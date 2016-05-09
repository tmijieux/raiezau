.PHONY = clean build recette build-tst run-tst run

SRC=rz/*.java rz/Exception/*.java

KLASS=$(SRC:.java=.class)
CLASS=$(foreach klass, $(KLASS), build/$(klass))

all: src

src: build
	mkdir -p build/
	javac -Xlint:unchecked -cp .:jar/jyaml-1.3.jar -g:source,lines,vars -d build/ $(SRC)

run:
	$(RM) completed/fifi.dat
	@java -classpath build/:jyaml/ rz.Client
run2:
	@java -classpath build/:jyaml/ rz.Client config2.ini
run3:
	$(RM) completed/fifi.dat
	@java -classpath build/:jyaml/ rz.Client config3.ini
run4:
	@java -classpath build/:jyaml/ rz.Client config4.ini
run5:
	@java -classpath build/:jyaml/ rz.Client config5.ini	
run6:
	@java -classpath build/:jyaml/ rz.Client config6.ini

demo:
	$(RM) completed_DEMO/* partial_DEMO/*
	@java -classpath build/:jyaml/ rz.Client configDEMO.ini
demo_serveur:
	@java -classpath build/:jyaml/ rz.Client configDEMO_SERVEUR.ini

debug:
	@jdb -sourcepath . -classpath build/:jyaml/ rz.Client

build:
	mkdir -p build/

jar: rz_client.jar

rz_client.jar: $(CLASS)
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
