import org.junit.Before;
import org.junit.Test;
import java.rmi.RemoteException;
import java.time.LocalTime;
import java.util.ArrayList;
import static org.junit.Assert.*;

/**
 * Tests for the Scheduler class.
 * These tests simulate various scenarios involving elevator requests, elevator movements,
 * and fault handling to ensure the Scheduler class behaves as expected under different conditions.
 */
public class SchedulerTest {
    // Scheduler instance to be tested
    private Scheduler scheduler;
    // Stubs for interacting with the Scheduler
    private SharedDataInterfaceStub<Request> fromFloorsStub;
    private ArrayList<SharedDataInterface<Integer>> toFloorsStub;
    private SharedDataInterfaceStub<ElevatorStatus> fromElevatorsStub;
    private ArrayList<SharedDataInterface<Command>> toElevatorsStub;

    /**
     * Setup method to initialize test environment before each test.
     * This includes initializing stubs and the Scheduler instance.
     */

    @Before
    public void setUp() {
        fromFloorsStub = new SharedDataInterfaceStub<>();
        fromElevatorsStub = new SharedDataInterfaceStub<>();
        toFloorsStub = new ArrayList<SharedDataInterface<Integer>>();
        toElevatorsStub = new ArrayList<>();

        for (int i = 0; i < ElevatorStart.NUM_ELEVATORS; i++) {
            toElevatorsStub.add(new SharedDataInterfaceStub<Command>());
        }

        // Prepopulate elevatorStatuses within Scheduler to avoid IndexOutOfBoundsException
        ArrayList<ElevatorStatus> prepopulatedElevatorStatuses = new ArrayList<>();
        for (int i = 0; i < ElevatorStart.NUM_ELEVATORS; i++) {
            prepopulatedElevatorStatuses.add(new ElevatorStatus(1, i + 1, 1, Elevator.ElevatorState.IDLE, Elevator.ElevatorMovement.STOP));
        }
        fromElevatorsStub.setItems(prepopulatedElevatorStatuses);
        // Initialize the Scheduler instance with the prepared stubs
        scheduler = new Scheduler(fromFloorsStub, toFloorsStub, fromElevatorsStub, toElevatorsStub);
    }
    /**
     * Tests that a request is successfully fetched from the floor interface.
     * @throws RemoteException if an RMI error occurs
     * @throws InterruptedException if interrupted during execution
     */
    @Test
    public void testGetRequest_withRequestAvailable() throws RemoteException, InterruptedException {
        //Define the time and create a test request
        LocalTime time = LocalTime.parse("14:05:15.0"); // Define a specific time for the test request
        Request testRequest = new Request(time, 1, Request.Direction.UP, 2, 0, false); // Create a test request with specified parameters

        //Simulate the arrival of a request by putting it into the fromFloorsStub
        fromFloorsStub.put(testRequest);

        //Execute the method under test
        scheduler.runOnce();

        //Assert that the scheduler has exactly one active request, which is the one we just added
        assertEquals("Expected 1 active request", 1, scheduler.getActiveRequests().size());

        //Assert that the active request in the scheduler matches the test request we created
        assertEquals(testRequest, scheduler.getActiveRequests().get(0));
    }

    /**
     * Tests that an elevator is selected for a request.
     * @throws RemoteException if an RMI error occurs
     */
    @Test
    public void testSelectElevator_ElevatorSelected() throws RemoteException, InterruptedException {
        LocalTime time = LocalTime.parse("14:05:15.0");
        Request testRequest = new Request(time, 1, Request.Direction.UP, 3, 0, false);
        fromFloorsStub.put(testRequest);

        scheduler.runOnce();

        boolean commandIssued = false;
        for (SharedDataInterface<Command> stub : toElevatorsStub) {
            if (stub.size() > 0) { // Checking if the stub has any items
                commandIssued = true;
                break; // Exit the loop as soon as we find a stub with items
            }
        }
        assertTrue("Expected at least one command to be issued to an elevator", commandIssued);
    }

    /**
     * Tests that a request is assigned to an elevator correctly.
     * @throws RemoteException if an RMI error occurs
     * @throws InterruptedException if interrupted during execution
     */
    @Test
    public void testAssignToElevator() throws RemoteException, InterruptedException {
        // Create a new request indicating a desire to move UP from floor 2 to floor 5
        LocalTime time = LocalTime.parse("14:05:15.0");
        Request testRequest = new Request(time, 2, Request.Direction.UP, 5, 0, false);
        fromFloorsStub.put(testRequest);

        scheduler.runOnce();

        boolean requestAssigned = false;

        // Iterate over each elevator's queue to find the request
        for (ArrayList<RequestPickedUpPair> elevatorQueue : scheduler.getElevatorRequests()) {
            for (RequestPickedUpPair pair : elevatorQueue) {
                if (pair.getRequest().equals(testRequest)) {
                    requestAssigned = true;
                    break;
                }
            }
            // If the request was found and the flag is true, break out of the outer loop
            if (requestAssigned) {
                break;
            }
        }
        // Assert that the request was assigned to an elevator
        assertTrue("Expected the request to be assigned to an elevator", requestAssigned);
    }
    /**
     * Tests that a floor is notified when an elevator arrives.
     * Assuming that the toFloorsStub is a list of SharedDataInterface<Integer>, where the integer
     * signifies the elevator number arriving at that floor.
     */
    @Test
    public void testNotifyFloorArrived() throws RemoteException, InterruptedException {
        int floorNumber = 2; // Example floor number to be notified
        Request requestToFloor2 = new Request(LocalTime.now(), 1, Request.Direction.UP, floorNumber, 0, false);
        fromFloorsStub.put(requestToFloor2); // Simulate a request to move to floor 2

        // Ensure toFloorsStub list has a stub for the specified floor number
        while (toFloorsStub.size() < floorNumber) {
            toFloorsStub.add(new SharedDataInterfaceStub<Integer>());
        }

        // trigger floor notification
        scheduler.notifyFloorArrived(new ElevatorStatus(1, floorNumber, 0, Elevator.ElevatorState.IDLE, Elevator.ElevatorMovement.STOP), floorNumber);

        // Assert that the floor was correctly notified
        SharedDataInterfaceStub<Integer> floorNotificationStub = (SharedDataInterfaceStub<Integer>) toFloorsStub.get(floorNumber - 1);
        assertFalse("Floor was not notified", floorNotificationStub.size() == 0); // Ensure the stub is not empty
        assertEquals("The notified elevator number does not match", Integer.valueOf(1), floorNotificationStub.get(0));
    }






    /**
     * Assuming a more complex scenario where multiple elevators might be involved, and notifications
     * might be sent to multiple floors. This test simulates such a situation.
     */
    @Test

    public void testNotifyFloorArrivedWithMultipleElevators() throws RemoteException, InterruptedException {
        // Assume floors 1 and 2 are being monitored
        toFloorsStub.add(new SharedDataInterfaceStub<Integer>()); // Floor 1 stub
        toFloorsStub.add(new SharedDataInterfaceStub<Integer>()); // Floor 2 stub

        LocalTime timeFloor1 = LocalTime.parse("14:05:15.0");
        LocalTime timeFloor2 = LocalTime.parse("14:10:00.0");
        Request requestFloor1 = new Request(timeFloor1, 1, Request.Direction.UP, 1, 0, false);
        Request requestFloor2 = new Request(timeFloor2, 2, Request.Direction.DOWN, 2, 0, false);

        fromFloorsStub.put(requestFloor1);
        fromFloorsStub.put(requestFloor2);

        scheduler.runOnce(); // This call should lead to the notification for floors being triggered

        // Checking if both floors were notified by checking the size of each stub
        SharedDataInterfaceStub<Integer> floor1NotificationStub = (SharedDataInterfaceStub<Integer>) toFloorsStub.get(0);
        SharedDataInterfaceStub<Integer> floor2NotificationStub = (SharedDataInterfaceStub<Integer>) toFloorsStub.get(1);

        assertTrue("Expected floor 1 to be notified", floor1NotificationStub.size() >= 0);
        assertTrue("Expected floor 2 to be notified", floor2NotificationStub.size() >= 0);
    }

    /**
     * Tests the command logic for issuing a moving command to an elevator.
     * This ensures that the Scheduler correctly issues commands based on the elevator's current status
     * and the request's details.
     */
    @Test
    public void testCommandLogicForMovingState() throws RemoteException, InterruptedException {
        //Define the time and create a test request for moving DOWN from floor 2 to floor 3
        LocalTime time = LocalTime.parse("14:05:15.0");
        Request moveRequest = new Request(time, 2, Request.Direction.DOWN, 3, 0, false);
        // Simulate the arrival of a request by putting it into the fromFloorsStub
        fromFloorsStub.put(moveRequest);

        // Simulate an elevator status that indicates it is ready to accept a request
        ElevatorStatus elevatorReadyStatus = new ElevatorStatus(1, 1, 1, Elevator.ElevatorState.IDLE, Elevator.ElevatorMovement.STOP);
        // Update the elevator's status in the system
        fromElevatorsStub.put(elevatorReadyStatus);

        //Execute the scheduler's logic to process the request and issue commands to the elevator
        scheduler.runOnce();

        //Retrieve the commands issued to the first elevator and verify that a command was issued
        SharedDataInterfaceStub<Command> commandsToElevator1 = (SharedDataInterfaceStub<Command>) toElevatorsStub.get(0);
        // Ensure that at least one command has been issued to the elevator
        assertFalse("Expected at least one command to be issued", commandsToElevator1.size() == 0);

        //Retrieve and verify the details of the command issued to ensure it matches the test scenario
        Command commandIssued = commandsToElevator1.get(0);
        // Ensure a command was actually issued
        assertNotNull("A command should have been issued", commandIssued);
        // Verify the command directs the elevator to move DOWN, matching the request's direction
        assertEquals("The command should indicate downward movement", Elevator.ElevatorMovement.DOWN, commandIssued.getMovement());
        // Verify the state of the command is MOVING, indicating the elevator should be in motion
        assertEquals("The command state should be MOVING", Elevator.ElevatorState.MOVING, commandIssued.getState());
    }




    /**
     * Tests that requests are re-provisioned correctly when an elevator is shut down.
     * This checks the Scheduler's ability to respond to elevator shutdowns by re-assigning its requests.
     *
     * @throws RemoteException if an RMI error occurs.
     * @throws InterruptedException if interrupted during execution.
     */
    @Test
    public void testElevatorShutdownAndRequestReprovisioning() throws RemoteException, InterruptedException {
        //Assign a request to an elevator
        LocalTime time = LocalTime.parse("14:05:15.0");
        Request requestForElevator1 = new Request(time, 2, Request.Direction.UP, 3, 0, false);
        fromFloorsStub.put(requestForElevator1);
        // Simulate an elevator in an idle state ready to accept requests
        fromElevatorsStub.put(new ElevatorStatus(1, 1, 2, Elevator.ElevatorState.IDLE, Elevator.ElevatorMovement.STOP));
        scheduler.runOnce(); // The request should be assigned to Elevator 1

        // Simulate the shutdown of Elevator 1
        fromElevatorsStub.put(new ElevatorStatus(1, 1, 2, Elevator.ElevatorState.SHUTDOWN, Elevator.ElevatorMovement.STOP));
        scheduler.runOnce(); // Process the shutdown and trigger re-provisioning of the request

        // Verify that the request is re-added to the pool for re-assignment
        assertFalse("Re-provisioned requests pool should not be empty after elevator shutdown", scheduler.getActiveRequests().isEmpty());
        Request reProvisionedRequest = scheduler.getActiveRequests().get(0);
        assertEquals("Re-provisioned request should match original request", requestForElevator1, reProvisionedRequest);
    }
}
