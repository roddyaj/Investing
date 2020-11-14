#!/bin/bash

set -e
[ -f target ] && rm -r target
cd src
javac -cp ../lib/commons-csv-1.8.jar -sourcepath . com/roddyaj/vf/Main.java -d ../target
cd ..
