#!/bin/bash

java -Xmx4096m -Djava.library.path=./resources/lib/osx_10_7/ -jar ./resources/ptree.jar $1 $2 $3
