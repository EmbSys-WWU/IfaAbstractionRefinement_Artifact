
SRC_DIR_SC2AST = ./de/fzi/sim/kascpar/sc2ast
SRC_DIR_UTIL =  ./de/fzi/sim/kascpar/util
XERCES=../bin/xercesImpl-2006-02-28.jar

all: jar_gen

jar_gen: java_comp
	cd ../bin; ./create_jar.sh; cd ../docs/examples; cp ../../bin/sc2ast.jar .;


java_comp:
	javac -g:none -d ../bin -classpath .:$(XERCES) $(SRC_DIR_SC2AST)/*.java
	javac -g:none -d ../bin -classpath .:$(XERCES) $(SRC_DIR_UTIL)/*.java

clean:
	cd ../bin; make clean

