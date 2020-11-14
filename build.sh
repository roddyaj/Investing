#!/bin/bash

set -e

# Clean
[ -f target ] && rm -r target

# Build
cd src
libs=$(ls ../lib/*.jar | grep -v source)
for lib in ${libs[@]}; do
  classpath+="${lib};"
done
javac -cp $classpath -sourcepath . com/roddyaj/vf/Main.java -d ../target
cd ..
