#!/bin/bash

#classpath+="../../.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.12.5/jackson-annotations-2.12.5.jar;"
#classpath+="../../.m2/repository/com/fasterxml/jackson/core/jackson-core/2.12.5/jackson-core-2.12.5.jar;"
#classpath+="../../.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.12.5/jackson-databind-2.12.5.jar;"
#classpath+="../../.m2/repository/org/apache/commons/commons-csv/1.9.0/commons-csv-1.9.0.jar;"
#classpath+="../../.m2/repository/com/squareup/okhttp3/okhttp/4.9.1/okhttp-4.9.1.jar;"
#classpath+="../../.m2/repository/com/squareup/okio/okio/2.8.0/okio-2.8.0.jar;"
#classpath+="../../.m2/repository/org/jetbrains/kotlin/kotlin-stdlib/1.4.10/kotlin-stdlib-1.4.10.jar;"
#classpath+='target/classes'
#java -cp $classpath com.roddyaj.invest.Main $@

args=$@
mvn exec:java -Dexec.args="${args}"
