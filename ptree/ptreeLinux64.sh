#!/bin/bash

java -Xmx4096m -Djava.library.path=./resources/lib/linux64/ -jar ./resources/ptree.jar $1 $2 $3
