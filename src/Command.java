import java.io.Serializable;

public class Command implements Serializable {
    private final Elevator.ElevatorState state;
    private final Elevator.ElevatorMovement movement;

    public Command(Elevator.ElevatorState state, Elevator.ElevatorMovement movement){
        this.state = state;
        this.movement = movement;
    }

    public Elevator.ElevatorState getState() {
        return state;
    }

    public Elevator.ElevatorMovement getMovement() {
        return movement;
    }

    @Override
    public String toString(){
        return String.format("State: %s, Movement: %s", state, movement);
    }
}
