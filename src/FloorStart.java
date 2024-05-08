import java.io.File;
import java.rmi.Naming;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;

public class FloorStart {
    public static final LocalTime ACTUAL_PROGRAM_START_TIME = LocalTime.now();

    private static LocalTime firstRequestTime;

    public static LocalTime getFirstRequestTime() {
        return firstRequestTime;
    }
    public static final String FLOOR_RMI_TO_SCHEDULER = "rmi://localhost/FloorsToScheduler";
    public static final int NUM_FLOORS = 22;

    public static void main(String[] args){
        //gets shared data to write to Scheduler
        SharedDataInterface<Request> toScheduler = null;
        try{
            toScheduler = (SharedDataInterface<Request>) Naming.lookup(FLOOR_RMI_TO_SCHEDULER);
        } catch(Exception e){
            Error.handleError(e);
        }

        //parses input file, gets shared data to read from Scheduler and starts Threads
        ArrayList<Request> allRequests = InputParser.parseFile(new File("src/InputFile.txt"));
        firstRequestTime = allRequests.get(0).getTime();
        System.out.println("Num requests: " + allRequests.size());
        for(int i = 0; i < NUM_FLOORS; i++){
            //gives requests to Floors
            ArrayList<Request> floorRequests = new ArrayList<>();
            for (Request request:allRequests){
                if (request.getSourceFloor() == i + 1){
                    floorRequests.add(request);
                }
            }

            //gets shared data to read from Scheduler
            SharedDataInterface<Integer> floorFromScheduler = null;
            try{
                floorFromScheduler = (SharedDataInterface<Integer>) Naming.lookup(String.format("rmi://localhost/Floor%dFromScheduler", i + 1));
            } catch (Exception e){
                Error.handleError(e);
            }

            // starts Threads
            Floor f = new Floor(i + 1, floorRequests, toScheduler, floorFromScheduler);
            Thread floorThread = new Thread(f, "Floor " + (i + 1));
            System.out.printf("Floor %d created\n", (i + 1));
            floorThread.start();
            try{
                Thread.sleep(100);
            } catch (InterruptedException e){
                Error.handleError(e);
            }
        }
    }
}
