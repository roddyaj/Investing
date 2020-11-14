#!/bin/bash

cd target ; java -cp ../lib/commons-csv-1.8.jar:. com/roddyaj/vf/Main $@ ; cd ..
