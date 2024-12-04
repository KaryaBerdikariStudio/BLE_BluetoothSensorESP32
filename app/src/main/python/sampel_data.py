
import numpy
import io

# Import sensor data ("short_walk.csv" or "long_walk.csv")
data = numpy.genfromtxt("short_walk.csv", delimiter=",", skip_header=1)

def timestamp ():
    data[:, 0]
def gyroscope ():
    data[:, 1:4]
def accelorometer():
    accelerometer = data[:, 4:7]
