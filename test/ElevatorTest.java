import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ElevatorTest {
    @Test
    void testRespondToCommand_ShutDown() {
        Elevator elevator = new Elevator(1, mock(SharedDataInterface.class), mock(SharedDataInterface.class));
        Command shutdownCommand = new Command(Elevator.ElevatorState.SHUTDOWN, Elevator.ElevatorMovement.STOP);
        elevator.respondToCommand(shutdownCommand);
        assertEquals(elevator.getCurrentState(), Elevator.ElevatorState.SHUTDOWN);
        assertTrue(elevator.isShutdown());
        assertFalse(elevator.isRespond());
    }

    @Test
    void testRespondToCommand_DoorsOpen() {
        Elevator elevator = new Elevator(1, mock(SharedDataInterface.class), mock(SharedDataInterface.class));
        Command doorsOpenCommand = new Command(Elevator.ElevatorState.DOORS_OPEN, Elevator.ElevatorMovement.STOP);
        elevator.respondToCommand(doorsOpenCommand);
        assertEquals(elevator.getCurrentState(), Elevator.ElevatorState.DOORS_OPEN);
        assertFalse(elevator.isShutdown());
        assertTrue(elevator.isRespond());
    }

    @Test
    void testRespondToCommand_Idle() {
        Elevator elevator = new Elevator(1, mock(SharedDataInterface.class), mock(SharedDataInterface.class));
        Command idleCommand = new Command(Elevator.ElevatorState.IDLE, Elevator.ElevatorMovement.STOP);
        elevator.respondToCommand(idleCommand);
        assertEquals(elevator.getCurrentState(), Elevator.ElevatorState.IDLE);
        assertFalse(elevator.isShutdown());
        assertTrue(elevator.isPrintedIdle());
    }

    @Test
    void testRespondToCommand_Null() {
        Elevator elevator = new Elevator(1, mock(SharedDataInterface.class), mock(SharedDataInterface.class));
        elevator.respondToCommand(null);
        assertEquals(elevator.getCurrentState(), Elevator.ElevatorState.IDLE);
        assertFalse(elevator.isShutdown());
        assertTrue(elevator.isPrintedIdle());
    }

    @Test
    void testApplyMovement_Up() {
        Elevator elevator = new Elevator(1, mock(SharedDataInterface.class), mock(SharedDataInterface.class));
        elevator.setCurrentDirection(Elevator.ElevatorMovement.UP);
        int initialFloor = elevator.getCurrentFloor();
        elevator.applyMovement();
        assertEquals(initialFloor + 1, elevator.getCurrentFloor());
    }

    @Test
    void testApplyMovement_Down() {
        Elevator elevator = new Elevator(1, mock(SharedDataInterface.class), mock(SharedDataInterface.class));
        elevator.setCurrentDirection(Elevator.ElevatorMovement.DOWN);
        int initialFloor = elevator.getCurrentFloor();
        elevator.applyMovement();
        assertEquals(initialFloor - 1, elevator.getCurrentFloor());
    }

    @Test
    void testApplyMovement_Stop() {
        Elevator elevator = new Elevator(1, mock(SharedDataInterface.class), mock(SharedDataInterface.class));
        elevator.setCurrentDirection(Elevator.ElevatorMovement.STOP);
        int initialFloor = elevator.getCurrentFloor();
        elevator.applyMovement();
        assertEquals(initialFloor, elevator.getCurrentFloor());
    }
}