all: org/nomi/pircbotNg/PircBotNg.class nomi/kvizbot/KvizBot.class

nomi/kvizbot/KvizBot.class: $(shell find nomi/kvizbot/ -name '*.java')
	javac -encoding UTF-8 $^

org/nomi/pircbotNg/PircBotNg.class: $(shell find org/nomi/pircbotNg/ -name '*.java')
	javac -encoding UTF-8 $^

KvizBot.jar: nomi/kvizbot/KvizBot.class
	jar cfm $@ MANIFEST.MF `find nomi/kvizbot/ -name '*.class'`

clean:
	find nomi/kvizbot -name '*.class' -print0 | xargs -0 $(RM)
	$(RM) KvizBot.jar

run: KvizBot.jar
	java -jar KvizBot.jar

.PHONY: run setup clean
