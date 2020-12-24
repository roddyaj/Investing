#!/bin/bash

# Clean
[ -d target ] && rm -r target

set -e

# Build
libs=$(ls lib/*.jar | grep -v source)
for lib in ${libs[@]}; do
  classpath+="${lib};"
done
javac -cp $classpath -sourcepath src src/com/roddyaj/invest/Main.java -d target
