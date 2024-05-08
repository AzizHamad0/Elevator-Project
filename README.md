# Elevator-Project
This project, designed for the SYSC 3303 course, simulates a multi-elevator system. It features a scheduler, multiple elevators, and floor subsystems, communicating over a network to coordinate the movement of elevators in response to user requests.

# Project Components

**SharedDataInterface.java:** Provides an interface for shared data access across different subsystems, ensuring consistency in the system's state and facilitating inter-process communication.
**Scheduler.java:** Responsible for coordinating all elevator movements. It receives requests from the floor subsystem, processes them according to the logic defined (like prioritizing, scheduling), and sends commands to the elevator subsystems.
**Request.java and RequestPickedUpPair.java:** Manage the data structure for elevator requests, including storing and retrieving request details, such as the originating floor, destination floor, and direction.
**MessageBuffer.java:**  manages message queues for asynchronous communication between the subsystems, ensuring that messages (like floor requests or elevator commands) are handled in a timely and organized manner.
**Main.java:** This file acts as the entry point for the entire simulation. It initializes the system, possibly setting up network connections, and starts the simulation by launching the scheduler and subsystems.
**InputParser.java:** Parses the InputFile.txt to extract elevator request events, which are then processed by the system to simulate real-time operation.
**InputFile.txt:** Contains pre-defined scenarios for elevator requests formatted with timestamps, floor numbers, directions, and target floors. Used for simulation inputs to test system responses under various conditions.
**GUI.java:** Provides a graphical user interface for the simulation, displaying the current state of elevators and floors, and allowing manual input for testing and demonstration purposes.
**Floor.java:** Simulates a floor in the building. It handles input from the floor buttons pressed by users and sends these requests to the scheduler. It also receives signals back from the scheduler to light up floor indicators.
**Error.java:**  Handles error conditions within the system, such as an elevator stuck between floors, a malfunctioning door, or communication errors.
**ElevatorStatus.java:**  stores and manages the state of an elevator, such as its current floor, whether its doors are open or closed, and its direction of travel.
**ElevatorStart.java and FloorStart.java:** responsible for initializing instances of the elevator and floor classes, setting initial states, and preparing them for operation within the simulation.
**Elevator.java:** Represents an elevator car. It processes commands from the scheduler to move between floors, open/close doors, and signal its current state back to the scheduler.
**Command.java:** Defines the set of commands or messages that can be sent between different parts of the system, such as commands to move the elevator, open/close doors, or update the floor indicators.

# Detailed Set Up and Test Instructions Download the Source Code
Install Java Development Kit (JDK) Install JUnit and Mockito 3.8 (for tests)
- org.junit.jupiter:junit-jupiter:5.9.0
- org.mockito:mockito-junit-jupiter:3.8.0 Compile the Code
Run Scheduler.java
After execution, the GUI should be displayed Run ElevatorStart.java
Run FloorStart.java

# Testing
Unit tests are available in the tests directory. Run these tests from the IDE or using a build tool configured for the project to ensure all components function correctly.

# Authors
Aziz Hamad (me)
and 4 other classmates

# Acknowledgements
- Thanks to Carleton University's Department of Systems and Computer Engineering for the project guidelines.
- Appreciation to course instructors and teaching assistants for their support.
