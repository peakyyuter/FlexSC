#!/bin/bash
if [ "$1" = "circuit" ] 
    then
        java -cp bin:lib/* oram.CountCircuitOramRec $2
elif [ "$1" = "trivial" ]
    then
        java -cp bin:lib/* oram.CountTrivialOram $2
else
    echo "2 args necessary: trivial/circuit N-value"
fi