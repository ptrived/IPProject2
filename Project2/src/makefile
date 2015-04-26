JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	AckHeader.java \
	DataPacket.java \
	Utils.java \
	SimpleFTPClient.java \
	SimpleFTPServer.java \
	SelectiveARQClient.java \
	SelectiveARQServer.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class