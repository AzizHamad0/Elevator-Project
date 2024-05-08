import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Scheduler implements Runnable {
    private final SharedDataInterface<Request> fromFloors;
    private final ArrayList<SharedDataInterface<Integer>> toFloors;
    private final SharedDataInterface<ElevatorStatus> fromElevators;
    private final ArrayList<SharedDataInterface<Command>> toElevators;
    private SchedulerState currentState = SchedulerState.IDLE;
    private final ArrayList<Request> activeRequests;
    private final ArrayList<ArrayList<RequestPickedUpPair>> elevatorRequests; // index is for elevator number, then next ArrayList stores the requests in sequential order
    private int unServicedRequest = -1;
    private LocalTime firstRequestTime;
    private LocalTime actualProgramStartScheduler;
    private boolean receivedLastRequest = false;
    private boolean doneRequests = false;
    private int numMovements = 0;
    private GUI gui;
    private ArrayList<ElevatorStatus> elevatorStatuses;
    private HashMap<Integer, LocalTime> elevatorTimers; // key = Elevator (placed once status is moving/open) // val is time when it was placed

    // the amount of time we'll wait in seconds before throwing a fault
    private final int openTime = (Elevator.ElevatorState.DOORS_OPEN.getSleepTime()/1000) + 5;
    private final int moveTime = (Elevator.ElevatorState.MOVING.getSleepTime()/1000) + 5;


    public enum SchedulerState {
        IDLE,
        PROCESSING_REQUEST,
        SELECTING_ELEVATOR,
    }
    public Scheduler(SharedDataInterface<Request> fromFloors, ArrayList<SharedDataInterface<Integer>> toFloors, SharedDataInterface<ElevatorStatus> fromElevators, ArrayList<SharedDataInterface<Command>> toElevators){
        this.fromFloors = fromFloors;
        this.toFloors = toFloors;
        this.fromElevators = fromElevators;
        this.toElevators = toElevators;
        this.activeRequests = new ArrayList<>();
        elevatorRequests = new ArrayList<>();
        elevatorStatuses = new ArrayList<>();
        elevatorTimers = new HashMap<>();
        for(int i = 0; i < ElevatorStart.NUM_ELEVATORS; i++){
            elevatorRequests.add(new ArrayList<>());
            elevatorStatuses.add(null);
        }
        gui = new GUI();
    }

    private void getRequest() throws RemoteException{
        Request getRequest = fromFloors.remove();
        if (getRequest != null) {
            setCurrentState(SchedulerState.PROCESSING_REQUEST);
            if(firstRequestTime == null){
                firstRequestTime = getRequest.getTime();
                actualProgramStartScheduler = LocalTime.now();
            }
            System.out.println();
            System.out.println("Got Request: " + getRequest);
            gui.handleFloorButtonPressed(getRequest.getSourceFloor(), getRequest.getDirection().toString());
            if(getRequest.isLastRequest()){
                receivedLastRequest = true;
                System.out.println("Received last request");
            }
            unServicedRequest = activeRequests.size();
            activeRequests.add(getRequest);
        }
        else{
            unServicedRequest = -1;
        }
    }

private void selectElevator() throws RemoteException {
    // update elevator statuses
    for (int i = 0; i < ElevatorStart.NUM_ELEVATORS; i++) {
        ElevatorStatus status = fromElevators.get(i);
        if (status == null) {
            continue; // Skip this iteration if status is null
        }
        
        if (!elevatorStatuses.contains(status)) {
            elevatorStatuses.set((status.getNumber() - 1), status); // Update elevator status
            System.out.println("\nGot status for Elevator " + status.getNumber() + ": " + status);
            gui.handleUpdateElevator(status.getNumber(), status.getCurrentFloor(), status.getCurrentState());

            // Clear the existing timers when a status is received
            elevatorTimers.remove(status.getNumber());

            // Place a timer if the state is MOVING or DOORS_OPEN, as these states have potential to raise faults
            if (status.getCurrentState() == Elevator.ElevatorState.MOVING) {
                elevatorTimers.put(status.getNumber(), LocalTime.now().plusSeconds(moveTime));
            }
            if (status.getCurrentState() == Elevator.ElevatorState.DOORS_OPEN) {
                elevatorTimers.put(status.getNumber(), LocalTime.now().plusSeconds(openTime));
            }

            // Reprovision requests if a shutdown status is received
            if (status.getCurrentState() == Elevator.ElevatorState.SHUTDOWN) {
                ArrayList<RequestPickedUpPair> requestsToReassign = elevatorRequests.get(status.getNumber() - 1);
                for (RequestPickedUpPair request : requestsToReassign) {
                    if (!request.isPickedUp()) {
                        System.out.println("Re-provisioning request: " + request.getRequest());
                        activeRequests.remove(request.getRequest());
                        fromFloors.put(request.getRequest());
                    }
                }
                requestsToReassign.clear();
            }
        }
    }

    // Assign requests to available elevators
    if (unServicedRequest != -1) {
        Request request = activeRequests.get(unServicedRequest);
        int lowestScore = Integer.MAX_VALUE;
        ElevatorStatus closestElevatorStatus = null;
        for (ElevatorStatus status : elevatorStatuses) {
            if (status != null && status.getCurrentState() != Elevator.ElevatorState.SHUTDOWN) {
                int score = calculateScore(status, request.getSourceFloor());
                if (score < lowestScore) {
                    closestElevatorStatus = status;
                    lowestScore = score;
                }
            }
        }
        try {
            if (closestElevatorStatus != null) {
                assignToElevator(closestElevatorStatus, request);
            } else {
                System.out.println("No elevators available: please restart the system");
            }
        } catch (Exception e) {
            System.out.println("Error in assigning elevator: " + e.getMessage());
        }
        unServicedRequest = -1;
    }
    checkEndOfProgram();
}


    public void checkEndOfProgram(){
        if(!doneRequests && receivedLastRequest && activeRequests.isEmpty()){
            Duration currentProgramDuration = Duration.between(actualProgramStartScheduler, LocalTime.now());
            LocalTime currentRelativeTime = firstRequestTime.plus(currentProgramDuration);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
            String formattedTime = currentRelativeTime.format(formatter);
            //Duration requestDuration = Duration.between(FloorStart.getFirstRequestTime(), requests.get(0).getTime());
            System.out.println("Finished last Request at " + formattedTime);
            System.out.println("Program Duration: " + currentProgramDuration.toSeconds() + " seconds");
            System.out.println("Total Number of Elevator Movements: " + numMovements);
            gui.handleCompletedTime(currentProgramDuration);
            gui.handleTotalMovements(String.valueOf(numMovements));
            doneRequests = true;
        }
    }

    public int calculateScore(ElevatorStatus status, int sourceFloor) {
        int score = 0;

        //ELEVATOR IS ON DESTINATION FLOOR
        if (status.getCurrentFloor() == sourceFloor && (status.getCurrentState() == Elevator.ElevatorState.IDLE || status.getCurrentState() == Elevator.ElevatorState.DOORS_OPEN)) { //check if elevator is already at destination
            score = -1;

            //ELEVATOR IS ON ITS WAY TO/PASSING BY DESTINATION FLOOR
        } else if ((status.getCurrentFloor() > sourceFloor && status.getMovement() == Elevator.ElevatorMovement.DOWN && status.getCurrentState() == Elevator.ElevatorState.MOVING) ||
                (status.getCurrentFloor() < sourceFloor && status.getMovement() == Elevator.ElevatorMovement.UP && status.getCurrentState() == Elevator.ElevatorState.MOVING)) { //check if elevator is above/below the destination and is on its way down/up
            if(status.getCurrentFloor() > sourceFloor) { //check if elevator is above source floor
                if(status.getDestinationFloor() > sourceFloor) { //elevator is going to a floor before reaching source floor
                    score = Math.abs(status.getCurrentFloor() - sourceFloor) * 10 + 60; //assign the amount of floors it has to pass through *10 //+120 because it has a stop
                } else { //elevator is going to a floor after reaching source floor
                    score = Math.abs(status.getCurrentFloor() - sourceFloor) * 10 - 1; //on the way to prior destination
                }
            } else {
                if (status.getDestinationFloor() < sourceFloor) { //elevator is going to a floor before reaching source floor
                    score = Math.abs(status.getCurrentFloor() - sourceFloor) * 10 + 60; //assign the amount of floors it has to pass through *10 //+120 because it has a stop
                } else { //elevator is going to a floor after reaching source floor
                    score = Math.abs(status.getCurrentFloor() - sourceFloor) * 10 - 1; //on the way to prior destination
                }
            }
            //ELEVATOR IS ON ITS WAY TO/PASSING BY DESTINATION FLOOR AND DOORS ARE OPEN
        } else if ((status.getCurrentFloor() > sourceFloor && status.getDestinationFloor() <= status.getCurrentFloor() && (status.getCurrentState() == Elevator.ElevatorState.DOORS_OPEN)) ||
                (status.getCurrentFloor() < sourceFloor && status.getDestinationFloor() >= status.getCurrentFloor() && (status.getCurrentState() == Elevator.ElevatorState.DOORS_OPEN))) { //check if elevator is above/below the destination and its doors are open
            if (status.getCurrentFloor() > sourceFloor) { //check if elevator is above source floor
                if (status.getDestinationFloor() > sourceFloor) { //elevator is going to a floor before reaching source floor
                    score = Math.abs(status.getCurrentFloor() - sourceFloor) * 10 + 120; //assign the amount of floors it has to pass through *10 //+120 because it has a stop
                } else { //elevator is going to a floor after reaching source floor
                    score = Math.abs(status.getCurrentFloor() - sourceFloor) * 10 + 61; //on the way to prior destination
                }
            } else { //elevator is below source floor
                if (status.getDestinationFloor() < sourceFloor) { //elevator is going to a floor before reaching source floor
                    score = Math.abs(status.getCurrentFloor() - sourceFloor) * 10 + 120; //assign the amount of floors it has to pass through *10 //+120 because it has a stop
                } else { //elevator is going to a floor after reaching source floor
                    score = Math.abs(status.getCurrentFloor() - sourceFloor) * 10 + 61; //on the way to prior destination
                }
            }
            //Prioritizing moving ones over idle ones if they are the same amount floors away from the destination floor

            //ELEVATOR IS IDLE AND ON DIFFERENT FLOOR
        } else if (status.getCurrentFloor() != sourceFloor && status.getCurrentState() == Elevator.ElevatorState.IDLE) { //check if elevator is idle and on different floor
            score = Math.abs(sourceFloor - status.getCurrentFloor()) * 10; //assign the amount of floors it has to pass through *10

            //ELEVATOR IS GOING OPPOSITE DIRECTION OF DESTINATION FLOOR
        } else if (status.getCurrentFloor() < sourceFloor && status.getMovement() == Elevator.ElevatorMovement.DOWN && (status.getCurrentState() == Elevator.ElevatorState.MOVING || status.getCurrentState() == Elevator.ElevatorState.DOORS_OPEN)) { //check if elevator is above the destination and is on its way down
            score = (Math.abs(sourceFloor - status.getCurrentFloor()) + Math.abs(sourceFloor - status.getDestinationFloor())) * 10; //assign the amount of floors it has to pass throught after reaching inital destination then going to destination*10
        } else if (status.getCurrentFloor() > sourceFloor && status.getMovement() == Elevator.ElevatorMovement.UP && (status.getCurrentState() == Elevator.ElevatorState.MOVING || status.getCurrentState() == Elevator.ElevatorState.DOORS_OPEN)) { //check if elevator is below the destination and is on its way up
            score = (Math.abs(status.getDestinationFloor() - status.getCurrentFloor()) + Math.abs(status.getDestinationFloor() - sourceFloor)) * 10; //assign the amount of floors it has to pass throught after reaching inital destination then going to destination*10

        } else if ((status.getCurrentFloor() > sourceFloor && status.getDestinationFloor() >= status.getCurrentFloor() && (status.getCurrentState() == Elevator.ElevatorState.DOORS_OPEN)) ||
                    (status.getCurrentFloor() < sourceFloor && status.getDestinationFloor() <= status.getCurrentFloor() && (status.getCurrentState() == Elevator.ElevatorState.DOORS_OPEN))) { //check if elevator is above/below the destination and its doors are open
            score = (Math.abs(status.getCurrentFloor() - status.getDestinationFloor()) + Math.abs(status.getDestinationFloor() - sourceFloor)) * 10 + 120;
                //Prioritizing moving ones over idle ones if they are the same amount floors away from the destination floor
            
            //ELEVATOR IS ON DEST FLOOR BUT ALREADY MOVING TO DIFFERENT FLOOR
        } else if (status.getCurrentFloor() == sourceFloor && status.getCurrentState() == Elevator.ElevatorState.MOVING) { //check if elevator is on the destination floor but already moving
            score = (Math.abs(status.getCurrentFloor() - status.getDestinationFloor()) + Math.abs(status.getDestinationFloor() - status.getCurrentFloor())) * 10; //assign the amount of floors it has to pass throught after reaching inital destination then going to destination*10
        } else {
            score = Integer.MAX_VALUE; //default score if elevator is bugged
        }
        return score;
    }
    
    private void assignToElevator(ElevatorStatus elevatorStatus, Request request) throws RemoteException{
        elevatorRequests.get(elevatorStatus.getNumber()-1).add(new RequestPickedUpPair(request, false));
        System.out.println();
        System.out.println("Assigned Request: " + request + " to Elevator " + elevatorStatus.getNumber());
    }

    void notifyFloorArrived(ElevatorStatus status, int floorNumber) throws RemoteException {
        if (floorNumber <= toFloors.size() && floorNumber > 0) { // Ensure index is within bounds
            SharedDataInterface<Integer> floorInterface = toFloors.get(floorNumber - 1);
            if (floorInterface != null) { // Additional null check for safety
                floorInterface.put(status.getNumber());
            }
        } else {
            System.out.println("Attempted to notify a floor that doesn't exist: " + floorNumber);
        }
    }

    private void commandLogic(ElevatorStatus elevatorStatus, RequestPickedUpPair requestPair) throws RemoteException{
        Request request = requestPair.getRequest();
        Elevator.ElevatorState state;
        Elevator.ElevatorMovement movement;
        int requestFloor;
        if (requestPair.isPickedUp()){
            requestFloor = request.getDestinationFloor();
        }
        else{
            requestFloor = request.getSourceFloor();
        }

        // check if elevator is arriving at a destination
        if(elevatorStatus.getCurrentFloor() > requestFloor){
            state = Elevator.ElevatorState.MOVING;
            movement = Elevator.ElevatorMovement.DOWN;
            numMovements++;
        }
        else if(elevatorStatus.getCurrentFloor() < requestFloor){
            state = Elevator.ElevatorState.MOVING;
            movement = Elevator.ElevatorMovement.UP;
            numMovements++;
        }
        else{
            state = Elevator.ElevatorState.DOORS_OPEN;
            movement = Elevator.ElevatorMovement.STOP;
            notifyFloorArrived(elevatorStatus, requestFloor);
            if(requestPair.isPickedUp()){
                elevatorRequests.get(elevatorStatus.getNumber()-1).remove(0);
                activeRequests.remove(request);
                gui.removeCarButtonPressed(elevatorStatus.getNumber(), request.getDestinationFloor());

            }
            else{
                gui.handleCarButtonPressed(elevatorStatus.getNumber(), request.getDestinationFloor());
                requestPair.setPickedUp(true);
                gui.removeFloorButtonPressed(request.getSourceFloor(), request.getDirection().toString());
            }
        }

        // if the request contains a nonzero fault value, send the fault as the state
        int fault = request.getFault();
        if (fault != 0)
        {
            switch (fault)
            {
                case 1: // Transient fault
                    state = Elevator.ElevatorState.TRANSIENT_FAULT;
                    break;
                case 2: // Hard fault
                    state = Elevator.ElevatorState.HARD_FAULT;
                    break;
                default:
                    System.out.println("Error: Fault value " + fault + " found, state not changing");
            }

            request.clearFault(); // remove the fault so it gets treated like a regular request in the future
        }

        Command command = new Command(state, movement);
        toElevators.get(elevatorStatus.getNumber()-1).put(command);
        System.out.println("Sent Command: " + command + " to Elevator " + elevatorStatus.getNumber());
        if(requestPair.isPickedUp()){
            elevatorStatus.setDestinationFloor(request.getDestinationFloor());
        } else {
            elevatorStatus.setDestinationFloor(request.getSourceFloor());
        }
    }

    public void commandElevators() throws RemoteException{
        int elevatorNumber = 1;
        for(SharedDataInterface<Command> commands:toElevators){
            if(commands.size() == 0 && !elevatorRequests.get(elevatorNumber-1).isEmpty()){
                commandSpecificElevator(elevatorNumber);
            }
            elevatorNumber++;
        }
    }

    private void commandSpecificElevator(int elevatorNumber) throws RemoteException{
        RequestPickedUpPair requestPickedUp = elevatorRequests.get(elevatorNumber-1).get(0);
        //System.out.println(requestPickedUp.getRequest());
        commandLogic(elevatorStatuses.get(elevatorNumber-1), requestPickedUp);
    }

    public void setCurrentState(SchedulerState state){
        currentState = state;
        System.out.println("Scheduler state: " + state);
    }

    public void checkTimers()
    {
        int[] removeList = new int[ElevatorStart.NUM_ELEVATORS];
        // check if any of the timers have expired
        // their values are the time we expect a response by
        // create a copy of the entries so we can modify the actual map
        Set<Map.Entry<Integer, LocalTime>> entries = new HashSet<>(elevatorTimers.entrySet());
        for (Map.Entry<Integer, LocalTime> timer : entries)
        {
            if (LocalTime.now().isAfter(timer.getValue()))
            {
                System.out.println("TIMED OUT:");
                handleFault(timer.getKey());
            }
        }
    }

    // receives an elevator id and handles the fault according to the elevator's last recorded state
    private void handleFault(int elevatorId)
    {
        ElevatorStatus status = elevatorStatuses.get(elevatorId-1); // adjust for index

        try
        {
            switch (status.getCurrentState())
            {
                case DOORS_OPEN:
                    // transient fault, assume something is blocking the door and re-send the open command
                    Command openCommand = new Command(Elevator.ElevatorState.DOORS_OPEN, Elevator.ElevatorMovement.STOP);
                    toElevators.get(elevatorId-1).put(openCommand);
                    elevatorTimers.put(elevatorId, LocalTime.now().plusSeconds(openTime));
                    System.out.printf("TRANSIENT FAULT: Resending elevator %d command: DOORS_OPEN\n", elevatorId);
                    gui.handleUpdateElevator(elevatorId, status.getCurrentFloor(), Elevator.ElevatorState.TRANSIENT_FAULT);
                    break;
                case MOVING:
                    // hard fault, tell the elevator to shut down
                    // fix it in person and restart the system when ready
                    Command shutdownCommand = new Command(Elevator.ElevatorState.SHUTDOWN, Elevator.ElevatorMovement.STOP);
                    toElevators.get(elevatorId-1).put(shutdownCommand);
                    System.out.printf("HARD FAULT: Shutting down elevator %d\n", elevatorId);
                    gui.handleUpdateElevator(elevatorId, status.getCurrentFloor(), Elevator.ElevatorState.HARD_FAULT);
                    elevatorTimers.remove(elevatorId);
                    activeRequests.remove(elevatorRequests.get(elevatorId-1).get(0).getRequest());
                    // set shutdown immediately.
                    elevatorStatuses.set(elevatorId-1, new ElevatorStatus(elevatorId-1, 0, 0, Elevator.ElevatorState.SHUTDOWN, Elevator.ElevatorMovement.STOP));
                    break;

                default:
                    System.out.printf("Not a handled state for faults: %s\n", status.getCurrentState());
            }
        }catch (Exception e)
        {
            Error.handleError(e);
        }
    }


    @Override
    public void run() {
        while(true){
            try {
                getRequest();
                selectElevator();
                commandElevators();

                checkTimers();
                Thread.sleep(100);
            } catch (RemoteException | InterruptedException e){
                Error.handleError(e);
            }
        }
    }

    public static void main(String[] args) {
        try{
            LocateRegistry.createRegistry(1099);

            MessageBuffer<Request> floorsIn = new MessageBuffer<>();
            Naming.rebind(FloorStart.FLOOR_RMI_TO_SCHEDULER, floorsIn);

            MessageBuffer<ElevatorStatus> elevatorsIn = new MessageBuffer<>();
            Naming.rebind(ElevatorStart.ELEVATOR_RMI_TO_SCHEDULER, elevatorsIn);

            ArrayList<SharedDataInterface<Integer>> floorsOut = new ArrayList<>();
            for(int i = 0; i < FloorStart.NUM_FLOORS; i++){
                MessageBuffer<Integer> floorOut = new MessageBuffer<>();
                Naming.rebind(String.format("rmi://localhost/Floor%dFromScheduler", i + 1), floorOut);
                floorsOut.add(floorOut);
            }

            ArrayList<SharedDataInterface<Command>> elevatorsOut = new ArrayList<>();
            for(int i = 0; i < ElevatorStart.NUM_ELEVATORS; i++){
                MessageBuffer<Command> elevatorOut = new MessageBuffer<>();
                Naming.rebind(String.format("rmi://localhost/Elevator%dFromScheduler", i + 1), elevatorOut);
                elevatorsOut.add(elevatorOut);
            }

            Scheduler s = new Scheduler(floorsIn, floorsOut, elevatorsIn, elevatorsOut);
            Thread schedulerThread = new Thread(s, "Scheduler");
            schedulerThread.start();
        } catch (RemoteException | MalformedURLException e) {
            Error.handleError(e);
        }
    }
    public List<Request> getActiveRequests() {
        return Collections.unmodifiableList(activeRequests);
    }

    public List<ArrayList<RequestPickedUpPair>> getElevatorRequests() {
        return Collections.unmodifiableList(elevatorRequests);
    }
    public void runOnce() throws RemoteException, InterruptedException {
        getRequest();
        selectElevator();
        commandElevators();

    }
}
