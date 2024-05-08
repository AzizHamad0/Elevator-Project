import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.rmi.RemoteException;
import java.time.LocalTime;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Floor class.
 * These tests verify the Floor class's interactions with a simulated scheduler, ensuring
 * that floor requests are correctly sent to and received from the scheduler.
 */
class FloorTest {
    private Floor floor; // The Floor instance to be tested
    private FakeSharedDataInterfaceRequest fakeScheduler; // A fake scheduler to simulate sending requests to the elevator system
    private FakeSharedDataInterfaceInteger fakeFromScheduler; // A fake scheduler interface to simulate receiving elevator arrival signals
    private Request testRequest; // A sample request to use in tests

    /**
     * Setup method to initialize test environment before each test case.
     */
    @BeforeEach
    void setUp() {
        // Initialize fake interfaces and a test request
        fakeScheduler = new FakeSharedDataInterfaceRequest();
        fakeFromScheduler = new FakeSharedDataInterfaceInteger();
        LocalTime time = LocalTime.parse("14:05:15.0");
        testRequest = new Request(time, 1, Request.Direction.UP, 2, 0, false); 

        // Initialize the Floor instance with the fake interfaces
        floor = new Floor(1, new ArrayList<>(), fakeScheduler, fakeFromScheduler);
    }

    /**
     * Tests that a request is successfully added to the scheduler.
     * @throws RemoteException If a remote invocation error occurs.
     */
    @Test
    void testPutInToSchedulerSuccessfully() throws RemoteException {
        // Send the request to the scheduler through the Floor instance
        floor.putInToScheduler(testRequest); 

        // Assert that the scheduler has exactly one request, indicating successful addition
        assertEquals(1, fakeScheduler.size(), "The request should be added to the scheduler.");
    }

    /**
     * Tests that the details of a request put into the scheduler match the expected values.
     */
    @Test
    void testPutInToSchedulerRequestDetails() {
        // Send the request to the scheduler
        floor.putInToScheduler(testRequest);

        Request scheduledRequest = null;
        try {
            // Retrieve the request from the fake scheduler to check its details
            scheduledRequest = fakeScheduler.get(0);
        } catch (Exception e) {
            // If retrieving the request fails, the test should fail
            fail("Retrieving the request failed.");
        }

        // Ensure the request retrieved is not null and its details match expectations
        assertNotNull(scheduledRequest, "Scheduled request should not be null.");
        assertEquals(Request.Direction.UP, scheduledRequest.getDirection(), "The direction of the request should be UP.");
    }

    /**
     * Tests receiving a true value from the scheduler, simulating an elevator's arrival.
     */
    @Test
    void testGetFromSchedulerTrue() {
        fakeFromScheduler.put(1); // 1 indicate arrival

        // Fetch the arrival signal
        Integer arrivalSignal = floor.getFromScheduler();

        // Assert the floor correctly interprets this signal
        assertTrue(arrivalSignal > 0, "Floor should receive a positive value indicating an elevator has arrived.");
    }

    /**
     * Tests receiving a false value from the scheduler, indicating no elevator has arrived.
     */
    @Test
    void testGetFromSchedulerFalse() {
        fakeFromScheduler.put(0); // 0 indicate no arrival

        // Fetch the arrival signal
        Integer arrivalSignal = floor.getFromScheduler();

        // Assert the floor correctly interprets the lack of an elevator arrival
        assertEquals(0, arrivalSignal, "Floor should receive a 0 value indicating no elevator has arrived.");
    }
}
