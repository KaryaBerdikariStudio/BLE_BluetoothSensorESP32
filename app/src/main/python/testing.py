import numpy as np
from matplotlib import animation, pyplot
from scipy.interpolate import interp1d
import imufusion
from dataclasses import dataclass

# Function to return position overtime from CSV file
def return_position_overtime(csv_file):
    # Import sensor data
    data = np.genfromtxt(csv_file, delimiter=",", skip_header=1)

    sample_rate = 400  # 400 Hz

    timestamp = data[:, 0]
    gyroscope = data[:, 1:4]
    accelerometer = data[:, 4:7]

    # Instantiate AHRS algorithms
    offset = imufusion.Offset(sample_rate)
    ahrs = imufusion.Ahrs()

    ahrs.settings = imufusion.Settings(imufusion.CONVENTION_NWU,
                                       0.5,  # gain
                                       2000,  # gyroscope range
                                       10,  # acceleration rejection
                                       0,  # magnetic rejection
                                       5 * sample_rate)  # rejection timeout = 5 seconds

    # Process sensor data
    delta_time = np.diff(timestamp, prepend=timestamp[0])

    # Initialize arrays to store velocity and position
    velocity = np.zeros((len(timestamp), 3))
    position = np.zeros((len(timestamp), 3))

    # Loop through each time sample to process the data
    for index in range(len(timestamp)):
        gyroscope[index] = offset.update(gyroscope[index])
        ahrs.update_no_magnetometer(gyroscope[index], accelerometer[index], delta_time[index])

        # Calculate acceleration (convert g to m/s²)
        acceleration = 9.81 * ahrs.earth_acceleration

        # Calculate velocity (integrating acceleration)
        if index > 0:  # Avoid integrating on the first sample
            velocity[index] = velocity[index - 1] + delta_time[index] * acceleration

        # Calculate position (integrating velocity)
        position[index] = position[index - 1] + delta_time[index] * velocity[index]

    # Return the timestamp and the corresponding position coordinates (X, Y, Z)
    return timestamp, position[:, 0], position[:, 1], position[:, 2]

# Import sensor data ("short_walk.csv" or "long_walk.csv")
csv_file = "short_walk.csv"  # Specify your CSV file here
timestamp, position_x, position_y, position_z = return_position_overtime(csv_file)

# Sample rate (400 Hz)
sample_rate = 400

# Plot sensor data
figure, axes = pyplot.subplots(nrows=6, sharex=True, gridspec_kw={"height_ratios": [6, 6, 6, 2, 1, 1]})
figure.suptitle("Sensors data, Euler angles, and AHRS internal states")

# Plot Gyroscope and Accelerometer Data
data = np.genfromtxt(csv_file, delimiter=",", skip_header=1)
timestamp = data[:, 0]
gyroscope = data[:, 1:4]
accelerometer = data[:, 4:7]

axes[0].plot(timestamp, gyroscope[:, 0], "tab:red", label="Gyroscope X")
axes[0].plot(timestamp, gyroscope[:, 1], "tab:green", label="Gyroscope Y")
axes[0].plot(timestamp, gyroscope[:, 2], "tab:blue", label="Gyroscope Z")
axes[0].set_ylabel("Degrees/s")
axes[0].grid()
axes[0].legend()

axes[1].plot(timestamp, accelerometer[:, 0], "tab:red", label="Accelerometer X")
axes[1].plot(timestamp, accelerometer[:, 1], "tab:green", label="Accelerometer Y")
axes[1].plot(timestamp, accelerometer[:, 2], "tab:blue", label="Accelerometer Z")
axes[1].set_ylabel("g")
axes[1].grid()
axes[1].legend()

# Process sensor data (using AHRS)
offset = imufusion.Offset(sample_rate)
ahrs = imufusion.Ahrs()
ahrs.settings = imufusion.Settings(imufusion.CONVENTION_NWU,
                                   0.5,  # gain
                                   2000,  # gyroscope range
                                   10,  # acceleration rejection
                                   0,  # magnetic rejection
                                   5 * sample_rate)  # rejection timeout = 5 seconds

delta_time = np.diff(timestamp, prepend=timestamp[0])
euler = np.empty((len(timestamp), 3))

for index in range(len(timestamp)):
    gyroscope[index] = offset.update(gyroscope[index])
    ahrs.update_no_magnetometer(gyroscope[index], accelerometer[index], delta_time[index])
    euler[index] = ahrs.quaternion.to_euler()

# Plot Euler angles
axes[2].plot(timestamp, euler[:, 0], "tab:red", label="Roll")
axes[2].plot(timestamp, euler[:, 1], "tab:green", label="Pitch")
axes[2].plot(timestamp, euler[:, 2], "tab:blue", label="Yaw")
axes[2].set_ylabel("Degrees")
axes[2].grid()
axes[2].legend()

# Velocity Plot
velocity = np.zeros((len(timestamp), 3))
acceleration = np.empty((len(timestamp), 3))

for index in range(len(timestamp)):
    acceleration[index] = 9.81 * ahrs.earth_acceleration
    if index > 0:
        velocity[index] = velocity[index - 1] + delta_time[index] * acceleration[index]

axes[3].plot(timestamp, velocity[:, 0], "tab:red", label="X")
axes[3].plot(timestamp, velocity[:, 1], "tab:green", label="Y")
axes[3].plot(timestamp, velocity[:, 2], "tab:blue", label="Z")
axes[3].set_ylabel("m/s")
axes[3].grid()
axes[3].legend()

# Plot Position
axes[4].plot(timestamp, position_x, "tab:red", label="X")
axes[4].plot(timestamp, position_y, "tab:green", label="Y")
axes[4].plot(timestamp, position_z, "tab:blue", label="Z")
axes[4].set_ylabel("m")
axes[4].grid()
axes[4].legend()

# Create 3D animation (position over time)
figure_3d = pyplot.figure(figsize=(10, 10))
axes_3d = pyplot.axes(projection="3d")
axes_3d.set_xlabel("X (meters)")
axes_3d.set_ylabel("Y (meters)")
axes_3d.set_zlabel("Z (meters)")

x = []
y = []
z = []

scatter = axes_3d.scatter(x, y, z)
fps = 30
samples_per_frame = int(sample_rate / fps)

# Update function for animation
def update(frame):
    index = frame * samples_per_frame
    axes_3d.set_title(f"{timestamp[index]:.3f} s")

    x.append(position_x[index])
    y.append(position_y[index])
    z.append(position_z[index])

    scatter._offsets3d = (x, y, z)

    if (min(x) != max(x)) and (min(y) != max(y)) and (min(z) != max(z)):
        axes_3d.set_xlim3d(min(x), max(x))
        axes_3d.set_ylim3d(min(y), max(y))
        axes_3d.set_zlim3d(min(z), max(z))

        axes_3d.set_box_aspect((np.ptp(x), np.ptp(y), np.ptp(z)))  # Aspect ratio

    return scatter

# Create the animation
anim = animation.FuncAnimation(figure_3d, update,
                               frames=int(len(timestamp) / samples_per_frame),
                               interval=1000 / fps,
                               repeat=False)

# Save the animation as a GIF
anim.save("position_animation.gif", writer=animation.PillowWriter(fps))

# Show the plot
pyplot.show()
