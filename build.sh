#!/bin/bash

# Clean
[ -d target ] && rm -r target

set -e

# Build
classpath+="../../.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.12.5/jackson-annotations-2.12.5.jar;"
classpath+="../../.m2/repository/com/fasterxml/jackson/core/jackson-core/2.12.5/jackson-core-2.12.5.jar;"
classpath+="../../.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.12.5/jackson-databind-2.12.5.jar;"
classpath+="../../.m2/repository/org/apache/commons/commons-csv/1.9.0/commons-csv-1.9.0.jar;"
javac -cp $classpath -sourcepath src/main/java src/main/java/com/roddyaj/invest/Main.java -d target/classes
