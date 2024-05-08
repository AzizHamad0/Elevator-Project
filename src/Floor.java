import java.rmi.RemoteException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;

public class Floor implements Runnable{
    private final int number;
    private final ArrayList<Request> requests; // assumed to be in sorted order of lowest time to greatest
    private final SharedDataInterface<Request> toScheduler;
    private final SharedDataInterface<Integer> fromScheduler;

    public Floor(int number, ArrayList<Request> requests, SharedDataInterface<Request> toScheduler, SharedDataInterface<Integer> fromScheduler){
        this.number = number;
        this.requests = requests;
        this.toScheduler = toScheduler;
        this.fromScheduler = fromScheduler;
    }
    @Override
    public void run(){
        while(true){
            if(!requests.isEmpty()){
                Duration currentProgramDuration = Duration.between(FloorStart.ACTUAL_PROGRAM_START_TIME, LocalTime.now());
                Duration requestDuration = Duration.between(FloorStart.getFirstRequestTime(), requests.get(0).getTime());

                if (currentProgramDuration.compareTo(requestDuration) >= 0) {
                    putInToScheduler(requests.remove(0));
                }
            }
            Integer msgFromScheduler = getFromScheduler();
            if (msgFromScheduler != 0){
                System.out.printf("Floor %d - Elevator %d arrived!\n", number, msgFromScheduler);
            }
            try{
                Thread.sleep(1000);
            }catch (InterruptedException e){
                Error.handleError(e);
            }
        }
    }

    public void putInToScheduler(Request request) {
        try{
            System.out.println("Floor " + number + " sent Request: " + request);
            toScheduler.put(request);
        } catch (RemoteException e){
            Error.handleError(e);
        }
    }
    public Integer getFromScheduler(){
        try{
            Integer msgFromScheduler = fromScheduler.remove();
            if(msgFromScheduler ==null){
                return 0;
            }
            return msgFromScheduler;
        } catch(RemoteException e){
            Error.handleError(e);
        }
        return 0;
    }
}
