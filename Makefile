.PHONY: all
all: JPlayer.jar

JPlayer.jar: JPlayer.class Restarter.class $(patsubst %,MPlayer_w%.png,16 24 32)
	jar cef JPlayer $@ $^

%.class: %.java
	javac $^

.PHONY: run
run: all
	java JPlayer http://streamer.psyradio.org:8010/

.PHONY: clean
clean:
	$(RM) *.class

MPlayer_2000.png:
	wget -O $@ http://upload.wikimedia.org/wikipedia/commons/thumb/8/81/MPlayer.svg/2000px-MPlayer.svg.png

MPlayer_2000.pnm: MPlayer_2000.png
	pngtopnm <$^ >$@

MPlayer_cropped.pnm: MPlayer_2000.pnm
	pnmcrop <$^ >$@

MPlayer_w%.pnm: MPlayer_cropped.pnm
	pnmscale -width $* <$^ >$@

MPlayer_w%.png: MPlayer_w%.pnm
	pnmtopng <$^ >$@
