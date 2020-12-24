#!/bin/bash

libs=$(ls lib/*.jar | grep -v source)
for lib in ${libs[@]}; do
  classpath+="${lib};"
done
classpath+='target'
java -cp $classpath com.roddyaj.invest.Main $@
