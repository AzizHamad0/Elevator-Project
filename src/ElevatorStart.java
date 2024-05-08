import java.rmi.Naming;

public class ElevatorStart {

    public static final String ELEVATOR_RMI_TO_SCHEDULER = "rmi://localhost/ElevatorsToScheduler";
    public static final int NUM_ELEVATORS = 4;

    public static void main(String[] args) {
        //gets shared data to write to Scheduler
        SharedDataInterface<ElevatorStatus> toScheduler = null;
        try{
            toScheduler = (SharedDataInterface<ElevatorStatus>) Naming.lookup(ELEVATOR_RMI_TO_SCHEDULER);
        } catch(Exception e){
            Error.handleError(e);
        }
        for (int i = 0; i < NUM_ELEVATORS; i++){
            SharedDataInterface<Command> elevatorFromScheduler = null;
            try{
                elevatorFromScheduler = (SharedDataInterface<Command>) Naming.lookup(String.format("rmi://localhost/Elevator%dFromScheduler", i + 1));
            } catch (Exception e){
                Error.handleError(e);
            }
            Elevator el = new Elevator(i + 1, toScheduler, elevatorFromScheduler);
            Thread elevator = new Thread(el, "Elevator " + (i + 1));
            elevator.start();
            try{
                Thread.sleep(100);
            } catch (InterruptedException e){
                Error.handleError(e);
            }
        }
    }
}
