JAVA_HOME = /usr/lib/jvm/java-1.6.0-sun
XERCES = $(STATE_HOME)/jars/xercesImpl-2006-02-28.jar
JAVAC_OP = $(JAVA_HOME)/bin/javac -classpath .:$(XERCES)
JAVA_OP = $(JAVA_HOME)/bin/java  -classpath .:$(XERCES)

all:
	@echo
	@echo " choose one of following targets:"
	@echo
	@echo " make install        : compile SC2AST tool"
	@echo " make examples       : execute the examples"
	@echo " make clean          : remove all temporary files"
	@echo " make clean_examples : remove all temporary files from the doc/examples folder"
	@echo 
	@echo 


install:
	cd src; make JAVAC="$(JAVAC_OP)"

examples:
	cd docs/examples; make -f Makefile_java JAVA_OP="$(JAVA_OP)"
clean: clean_examples
	cd src; make clean

clean_examples:
	cd docs/examples; make -f Makefile_java clean