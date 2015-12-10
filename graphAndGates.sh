#!/bin/bash
if [ "$1" = "circuit" ] 
    then
        python graphing/graphAndGates.py AndGateResults/circuitAndGateResults.txt
elif [ "$1" = "trivial" ]
    then
        python graphing/graphAndGates.py AndGateResults/trivialAndGateResults.txt
else
    echo "1 args necessary: trivial/circuit "
fi