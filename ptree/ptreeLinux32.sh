#!/bin/bash

java -Xmx1024m -Djava.library.path=./resources/lib/linux32/ -jar ./resources/ptree.jar $1 $2 $3
