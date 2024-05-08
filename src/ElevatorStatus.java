import java.io.Serializable;

public class ElevatorStatus implements Serializable {
    private final int number;
    private final int currentFloor;
    private int destinationFloor;
    private final Elevator.ElevatorState currentState;
    private final Elevator.ElevatorMovement movement;

    public ElevatorStatus(int number, int currentFloor, int destinationFloor, Elevator.ElevatorState currentState, Elevator.ElevatorMovement movement){
        this.number = number;
        this.currentFloor = currentFloor;
        this.destinationFloor = destinationFloor;
        this.currentState = currentState;
        this.movement = movement;
    }

    public int getNumber(){return number;}

    public int getCurrentFloor() {
        return currentFloor;
    }

    public int getDestinationFloor() { return destinationFloor; }
    public void setDestinationFloor(int destinationFloor) { this.destinationFloor = destinationFloor; }
    
    public Elevator.ElevatorState getCurrentState() {
        return currentState;
    }

    public Elevator.ElevatorMovement getMovement() {
        return movement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElevatorStatus that = (ElevatorStatus) o;
        return number == that.number &&
                currentFloor == that.currentFloor &&
                currentState == that.currentState &&
                movement == that.movement;
    }
    @Override
    public String toString(){
        return String.format("Number: %d, Current Floor: %d, Current State: %s, Movement: %s", number, currentFloor, currentState, movement);
    }
}
