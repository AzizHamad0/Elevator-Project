import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {
    @Test
    void testConstructor() {
        Elevator.ElevatorState state = Elevator.ElevatorState.IDLE;
        Elevator.ElevatorMovement movement = Elevator.ElevatorMovement.UP;
        Command command = new Command(state, movement);
        assertEquals(state, command.getState());
        assertEquals(movement, command.getMovement());
    }

    @Test
    void testToString() {
        Elevator.ElevatorState state = Elevator.ElevatorState.IDLE;
        Elevator.ElevatorMovement movement = Elevator.ElevatorMovement.UP;
        Command command = new Command(state, movement);
        assertEquals("State: IDLE, Movement: UP", command.toString());
    }
}
