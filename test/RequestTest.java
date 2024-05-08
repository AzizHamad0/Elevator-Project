import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {
    @Test
    void testRequest() {
        LocalTime time = LocalTime.parse("14:05:25.0");
        int sourceFloor = 3;
        Request.Direction direction = Request.Direction.UP;
        int destinationFloor = 5;
        int fault = 0;

        // Create Request object using the constructor
        Request request = new Request(time, sourceFloor, direction, destinationFloor, fault,false);

        // Test that the fields are initialized correctly
        assertEquals(time, request.getTime());
        assertEquals(sourceFloor, request.getSourceFloor());
        assertEquals(direction, request.getDirection());
        assertEquals(destinationFloor, request.getDestinationFloor());
        assertEquals(fault, request.getFault());
    }

    @Test
    void testRequestToString() {
        LocalTime time = LocalTime.parse("14:05:25.0");
        Request request = new Request(time, 3, Request.Direction.UP, 5, 0,false);
        String expectedToString = "Time: 14:05:25.000, Source Floor: 3, Direction: UP, Destination Floor: 5";
        assertEquals(expectedToString, request.toString());
    }
}
