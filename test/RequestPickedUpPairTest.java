import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class RequestPickedUpPairTest {
    @Test
    void testRequestPickedUpPair_false() {
        LocalTime time = LocalTime.parse("14:05:25.0");
        Request request = new Request(time, 3, Request.Direction.UP, 5, 0, false);

        RequestPickedUpPair requestPickedUpPair = new RequestPickedUpPair(request, false);
        assertEquals(request, requestPickedUpPair.getRequest());
        assertFalse(requestPickedUpPair.isPickedUp());
    }

    @Test
    void testRequestPickedUpPair_true() {
        LocalTime time = LocalTime.parse("14:05:25.0");
        Request request = new Request(time, 3, Request.Direction.UP, 5, 0,false);

        RequestPickedUpPair requestPickedUpPair = new RequestPickedUpPair(request, true);
        assertEquals(request, requestPickedUpPair.getRequest());
        assertTrue(requestPickedUpPair.isPickedUp());
    }

    @Test
    void testRequestPickedUpPair_set() {
        LocalTime time = LocalTime.parse("14:05:25.0");
        Request request = new Request(time, 3, Request.Direction.UP, 5, 0,false);

        RequestPickedUpPair requestPickedUpPair = new RequestPickedUpPair(request, true);
        assertEquals(request, requestPickedUpPair.getRequest());
        assertTrue(requestPickedUpPair.isPickedUp());
        requestPickedUpPair.setPickedUp(false);
        assertFalse(requestPickedUpPair.isPickedUp());
    }
}
