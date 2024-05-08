import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ElevatorStatusTest {
    @Test
    void testElevatorStatus() {
        int number = 1;
        int currentFloor = 5;
        int destinationFloor =1;
        Elevator.ElevatorState currentState = Elevator.ElevatorState.IDLE;
        Elevator.ElevatorMovement movement = Elevator.ElevatorMovement.UP;

        // Create an ElevatorStatus object using the constructor
        ElevatorStatus status = new ElevatorStatus(number, currentFloor, destinationFloor,currentState, movement);

        // Test that the fields are initialized correctly
        assertEquals(number, status.getNumber());
        assertEquals(currentFloor, status.getCurrentFloor());
        assertEquals(currentState, status.getCurrentState());
        assertEquals(movement, status.getMovement());
    }
    @Test
    public void testEquals_SameObject() {
        ElevatorStatus status1 = new ElevatorStatus(1, 5,1, Elevator.ElevatorState.IDLE, Elevator.ElevatorMovement.UP);

        // Test when compared with itself
        assertTrue(status1.equals(status1));
    }

    @Test
    public void testEquals_NullObject() {
        ElevatorStatus status1 = new ElevatorStatus(1, 5,1, Elevator.ElevatorState.IDLE, Elevator.ElevatorMovement.UP);

        // Test when compared with null
        assertFalse(status1.equals(null));
    }

    @Test
    public void testEquals_DifferentClass() {
        ElevatorStatus status1 = new ElevatorStatus(1, 5,1, Elevator.ElevatorState.IDLE, Elevator.ElevatorMovement.UP);

        // Test when compared with an object of a different class
        assertFalse(status1.equals("some string"));
    }

    @Test
    public void testEquals_EqualObjects() {
        ElevatorStatus status1 = new ElevatorStatus(1, 5,21, Elevator.ElevatorState.IDLE, Elevator.ElevatorMovement.UP);
        ElevatorStatus status2 = new ElevatorStatus(1, 5,1, Elevator.ElevatorState.IDLE, Elevator.ElevatorMovement.UP);

        // Test when compared with an equal object
        assertTrue(status1.equals(status2));
    }

    @Test
    public void testEquals_UnequalObjects() {
        ElevatorStatus status1 = new ElevatorStatus(1, 5,2, Elevator.ElevatorState.IDLE, Elevator.ElevatorMovement.UP);
        ElevatorStatus status2 = new ElevatorStatus(2, 5,2, Elevator.ElevatorState.IDLE, Elevator.ElevatorMovement.UP);

        // Test when compared with an unequal object
        assertFalse(status1.equals(status2));
    }

    @Test
    public void testToString() {
        ElevatorStatus status = new ElevatorStatus(1, 5,3, Elevator.ElevatorState.IDLE, Elevator.ElevatorMovement.UP);
        String expectedToString = "Number: 1, Current Floor: 5, Current State: IDLE, Movement: UP";
        assertEquals(expectedToString, status.toString());
    }
}
