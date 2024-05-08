import java.rmi.RemoteException;

public class Elevator implements Runnable{
    private final int number;
    private int currentFloor;
    private int destinationFloor;
    private ElevatorState currentState = ElevatorState.IDLE;
    private ElevatorMovement currentDirection = ElevatorMovement.STOP;
    private ElevatorStatus currentStatus;
    private final SharedDataInterface<ElevatorStatus> toScheduler;
    private final SharedDataInterface<Command> fromScheduler;
    private boolean printedIdle = false;
    private boolean shutdown;
    private boolean respond;
    public enum ElevatorState{
        IDLE(1000),
        DOORS_OPEN(3000 + 5000 + 3000),
        MOVING(10000),

        TRANSIENT_FAULT(20000),
        HARD_FAULT(20000),
        SHUTDOWN(1000);


        private final int sleepTime;
         ElevatorState(int sleepTime){
             this.sleepTime = sleepTime;
         }
         public int getSleepTime(){
             return sleepTime;
         }
    }
    public enum ElevatorMovement{
        UP,
        DOWN,
        STOP
    }
    public Elevator(int number, SharedDataInterface<ElevatorStatus> toScheduler, SharedDataInterface<Command> fromScheduler){
        this.number = number;
        currentFloor = 1;
        destinationFloor = 1;
        this.toScheduler = toScheduler;
        this.fromScheduler = fromScheduler;
        currentStatus = new ElevatorStatus(number, currentFloor, destinationFloor, currentState, currentDirection);

        shutdown = false; // keeps the thread alive
        respond = true; // turns off when we get a hard fault
    }

    public Command getCommand() throws RemoteException {
        return fromScheduler.remove();
    }

    public void respondToCommand(Command command){
        if (command != null) {
            System.out.println("Elevator " + number + " got Command: " + command);
            setCurrentState(command.getState());
            currentDirection = command.getMovement();

            // check fault first
            if (currentState.equals(ElevatorState.SHUTDOWN))
            {
                System.out.println("SHUTDOWN DETECTED: SHUTTING DOWN...");
                shutdown = true;
                respond = true;
                try
                {
                    sendStatus();
                }catch (Exception e)
                {
                    Error.handleError(e);
                }
                respond = false;
            }

            if (currentState.equals(ElevatorState.DOORS_OPEN)) {
                System.out.println("Elevator " + number + " state: DOORS_OPEN at floor " + currentFloor);
            } else if (currentState.equals(ElevatorState.IDLE)) {
                if (!printedIdle) {
                    System.out.println("Elevator " + number + " state: IDLE");
                    printedIdle = true;
                }
            } else {
                System.out.printf("Elevator %d, state %s, direction %s, current floor %d\n", number, currentState, currentDirection, currentFloor);
            }
        }
        else{
            setCurrentState(ElevatorState.IDLE);
        }
    }

    public void applyMovement(){
        if(!currentDirection.equals(ElevatorMovement.STOP)){
            //printedIdle = false;
            if(currentDirection.equals(ElevatorMovement.UP)){
                currentFloor++;
            }
            else{
                currentFloor--;
            }
        }
    }

    public void sendStatus() throws RemoteException{
//        System.out.println("sending status");
        if (!respond) return;

        try{
            synchronized (toScheduler) {
                toScheduler.remove(currentStatus);
                currentStatus = new ElevatorStatus(number, currentFloor, destinationFloor, currentState, currentDirection);
                toScheduler.put(currentStatus);
            }
        } catch (RemoteException e){
            Error.handleError(e);
        }
    }

    public void setCurrentState(ElevatorState state){
        currentState = state;
        if (!printedIdle){
            System.out.println("Elevator " + number + " state: " + state);
            printedIdle = true;
        }
    }

    //FOR TESTS
    public ElevatorState getCurrentState() { return currentState; }
    public boolean isShutdown() { return shutdown; }
    public boolean isRespond() { return respond; }
    public boolean isPrintedIdle() { return printedIdle; }
    public void setCurrentDirection(ElevatorMovement c) { currentDirection = c; }
    public int getCurrentFloor() { return currentFloor; }

    private void handleState()
    {
        try
        {
            switch (currentState)
            {
                case MOVING:
                    if (!respond)
                    {
                        // skip until we're responsive again, only move if we're letting the scheduler know
                        // delay a bit so the scheduler isn't completely flooded with attempts before timeout
                        Thread.sleep(1000);
                        return;
                    }
                    applyMovement();
                    sendStatus();
                    Thread.sleep(currentState.getSleepTime());
                    break;
                case TRANSIENT_FAULT:
                    // open doors for an extended period of time
                    System.out.printf("Elevator %d Transient fault simulating: Opening doors and not closing\n", number);
                    setCurrentState(ElevatorState.DOORS_OPEN); // status to send scheduler to start its timer
                    currentDirection = ElevatorMovement.STOP;
                    sendStatus(); // send state as though the elevator opened its doors

                    respond = false; // stop responding until next open_door request
                    break;
                case HARD_FAULT:
                    // act as though moving but sensor broke/didn't trigger
                    setCurrentState(ElevatorState.MOVING);
                    sendStatus();
                    respond = false;
                    break;
                case DOORS_OPEN:
                    respond = true; // in the case of a transient fault, get respond = true again
                    // no need to break, handle regularly
                default: // other cases such as open doors or idle
                    sendStatus();
                    Thread.sleep(currentState.getSleepTime());
            }
        } catch (Exception e){
            Error.handleError(e);
        }
    }


    @Override
    public void run(){
        while (!shutdown){ // checks if a shutdown condition (hard fault) has triggered
            try{
                Command command = getCommand();
                respondToCommand(command);
                handleState();
            } catch (Exception e){
                Error.handleError(e);
            }
        }

        System.out.printf("Elevator %d shutting down...\n", number);
    }
}
