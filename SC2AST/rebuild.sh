#!/bin/bash

jjtree -OUTPUT_DIRECTORY=src/de/fzi/sim/kascpar/sc2ast src/SC2AST.jjt

if [ $? != 0 ] ; then
    echo "jjtree failed"
    exit
fi

PWD=`pwd`

cd src/de/fzi/sim/kascpar/sc2ast
rm -f SCParserVisitor.java
cp SCParserVisitor.save SCParserVisitor.java

javacc SC2AST.jj

if [ $? != 0 ] ; then
    echo "jjtree failed"
    exit
fi

PWD=`pwd`

#switch to ./src
cd ../../../../..
make java_comp

#switch to ./bin
cd ../bin
./create_jar.sh
