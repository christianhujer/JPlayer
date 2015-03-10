.PHONY: all
all: JPlayer.class

%.class: %.java
	javac $^

.PHONY: run
run: all
	java JPlayer http://streamer.psyradio.org:8010/

.PHONY: clean
clean:
	$(RM) *.class
