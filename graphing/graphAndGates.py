import matplotlib.pyplot as plt
import sys
import math

def graphAndGates(source):
    try:
        with open(source) as f:
            data = [x.split(" ") for x in f.read().replace("\n",",").split(",")]
        x = []
        y = []
        for i in range(len(data)-1):
            y.append(math.log(int(data[i][1]),10))
            x.append(int(data[i][0]))
        print y
        plt.plot(x,y)
        plt.xlabel('N Value')
        plt.ylabel('AND Gates')
        plt.show()
    except IOError:
        print "ERROR: "+sys.argv[1]+" file non-existent"
        print "RUN: ./countAndGates.sh trivial/circuit N-value to generate"

graphAndGates(sys.argv[1])