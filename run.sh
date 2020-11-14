#!/bin/bash

cd target
libs=$(ls ../lib/*.jar | grep -v source)
for lib in ${libs[@]}; do
  classpath+="${lib};"
done
classpath+='.'
java -cp $classpath com.roddyaj.vf.Main $@
cd ..
