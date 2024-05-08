import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Request implements Serializable {
    private LocalTime time;
    private int sourceFloor;
    private Direction direction;
    private int destinationFloor;
    private int fault;
    private boolean isLastRequest;
    public enum Direction{
        UP,
        DOWN
    }
    public Request(LocalTime time, int sourceFloor, Direction direction, int destinationFloor, int fault, boolean isLastRequest){
        this.time = time;
        this.sourceFloor = sourceFloor;
        this.direction = direction;
        this.destinationFloor = destinationFloor;
        this.fault = fault;
        this.isLastRequest = isLastRequest;
    }
    public boolean isLastRequest(){
        return isLastRequest;
    }
    public void setIsLastRequest(boolean isLastRequest){
        this.isLastRequest = isLastRequest;
    }

    public LocalTime getTime() {
        return time;
    }

    public int getSourceFloor() {
        return sourceFloor;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    public void clearFault()
    {
        fault = 0;
    }
    public int getFault(){
        return fault;
    }
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        String formattedTime = time.format(formatter);
        return String.format("Time: %s, Source Floor: %d, Direction: %s, Destination Floor: %d", formattedTime, sourceFloor, direction, destinationFloor);
    }
}
