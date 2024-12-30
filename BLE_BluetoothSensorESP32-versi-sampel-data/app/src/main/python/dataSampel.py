from dataclasses import dataclass
from matplotlib import animation
from scipy.interpolate import interp1d
import matplotlib.pyplot as pyplot
import numpy

# Import sensor data ("short_walk.csv" or "long_walk.csv")
data = numpy.genfromtxt("short_walk.csv", delimiter=",", skip_header=1)

sample_rate = 400  # 400 Hz

timestamp = data[:, 0]
gyroscope = data[:, 1:4]
accelerometer = data[:, 4:7]

# Plot sensor data
figure, axes = pyplot.subplots(nrows=6, sharex=True, gridspec_kw={"height_ratios": [6, 6, 6, 2, 1, 1]})

