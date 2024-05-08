import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;

public class GUI{
    private JFrame frame;
    private JPanel floorNums;
    private JPanel elevatorNums;
    private JPanel carButtonPanel;
    private JPanel floorButtonPanel;
    private JPanel elevatorGrid;
    private JPanel stats;
    private JButton[][] elevatorCells; // first cell represents floor, second represents elevator
    private JLabel[] carButtonLabels;
    private JLabel[] floorButtonLabels;
    private JLabel completedTime;
    private JLabel totalMovements;
    private ArrayList<Integer>[] carButtons;
    private ArrayList<String>[] floorButtons;
    private final int FLOORS = 22;
    private final int ELEVATORS = 4;

    public GUI(){
        carButtons = new ArrayList[ELEVATORS];
        floorButtons = new ArrayList[FLOORS];

        initializeArray(carButtons);
        initializeArray(floorButtons);

        frame = new JFrame("Elevator Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setSize(1200,800);

        createElevatorNums();
        createFloorNums();
        createElevatorGrid();
        createCarButtons();
        createFloorButtons();
        createStatsPanel();


        // Adding panels
        frame.add(elevatorNums, BorderLayout.NORTH);
        frame.add(floorNums, BorderLayout.WEST);
        frame.add(elevatorGrid, BorderLayout.CENTER);
        frame.add(carButtonPanel, BorderLayout.SOUTH);
        frame.add(floorButtonPanel, BorderLayout.EAST);
        frame.add(stats);

        frame.setVisible(true);
    }

    // create elevatorNums panel
    private void createElevatorNums(){
        elevatorNums = new JPanel(null);
        elevatorNums.setBounds(0, 0, 1200, 100);

        JLabel elevatorTitle = new JLabel("Elevators");
        elevatorTitle.setBounds(500, 0, 200, 80);
        elevatorTitle.setFont(new Font(elevatorTitle.getName(), Font.PLAIN, 30));

        // Creates the elevator id labels
        for(int i = 0; i<4; i++){
            JLabel elevatorId = new JLabel(String.valueOf(i+1));
            elevatorId.setBounds(275+(i*175), 55, 100, 50);
            elevatorId.setFont(new Font(elevatorId.getName(), Font.PLAIN, 25));
            elevatorNums.add(elevatorId);
        }

        JLabel floorButtonLabel = new JLabel("Floor Buttons:");
        floorButtonLabel.setBounds(900, 55, 150, 50);
        floorButtonLabel.setFont(new Font(floorButtonLabel.getName(), Font.PLAIN, 15));

        elevatorNums.add(floorButtonLabel);
        elevatorNums.add(elevatorTitle);
    }

    // create floorNums panel
    private void createFloorNums() {
        floorNums = new JPanel(null);
        floorNums.setBounds(0, 100, 200, 700);

        JLabel floorTitle = new JLabel("Floors");
        floorTitle.setBounds(0, 200, 200, 80);
        floorTitle.setFont(new Font(floorTitle.getName(), Font.PLAIN, 30));

        for(int i = 0; i < 22; i++){
            JLabel floorId = new JLabel(String.valueOf(i+1));
            floorId.setBounds(180, 488-(i*23), 30, 30);
            floorId.setFont(new Font(floorId.getName(), Font.PLAIN, 15));
            floorNums.add(floorId);
        }
        JLabel carButtonLabel = new JLabel("Car Buttons:");
        carButtonLabel.setBounds(100, 506, 150, 30);
        carButtonLabel.setFont(new Font(carButtonLabel.getName(), Font.PLAIN, 15));

        floorNums.add(carButtonLabel);
        floorNums.add(floorTitle);
    }

    private void createElevatorGrid(){
        elevatorGrid = new JPanel(new GridLayout(FLOORS, ELEVATORS));
        elevatorGrid.setBounds(200, 100, 700, 525);

        elevatorCells = new JButton[FLOORS][ELEVATORS];

        for(int rows = 0; rows < FLOORS; rows++){
            for(int cols = 0; cols < ELEVATORS; cols++){
                JButton elevatorCell = new JButton();
                elevatorCell.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                this.elevatorCells[rows][cols] = elevatorCell;
                this.elevatorGrid.add(elevatorCell);
            }
        }
    }

    public void createCarButtons(){
        carButtonPanel = new JPanel(null);
        carButtonPanel.setBounds(200, 625, 700, 150);

        carButtonLabels = new JLabel[ELEVATORS];
        for(int i = 0; i<ELEVATORS; i++){
            carButtonLabels[i] = new JLabel();
            carButtonLabels[i].setBounds(70+(i*170), -40, 100, 100);
            carButtonLabels[i].setFont(new Font(carButtonLabels[i].getName(), Font.PLAIN, 13));
            carButtonPanel.add(carButtonLabels[i]);
        }
    }

    public void createFloorButtons(){
        floorButtonPanel = new JPanel(null);
        floorButtonPanel.setBounds(900, 100, 300, 525);

        floorButtonLabels = new JLabel[FLOORS];
        for(int i = 0; i<FLOORS; i++){
            floorButtonLabels[i] = new JLabel();
            floorButtonLabels[i].setBounds(0, 478-(i*23), 100, 50);
            floorButtonLabels[i].setFont(new Font(floorButtonLabels[i].getName(), Font.PLAIN, 13));
            floorButtonPanel.add(floorButtonLabels[i]);
        }
    }

    public void createStatsPanel(){
        stats = new JPanel(null);
        stats.setBounds(900, 625, 300, 150);

        completedTime = new JLabel("Completed Time: ");
        completedTime.setBounds(0,0,300,75);

        totalMovements = new JLabel ("Total Movements: ");
        totalMovements.setBounds(0,25, 200, 100);

        stats.add(completedTime);
        stats.add(totalMovements);
    }

    // works
    public void handleCarButtonPressed(int elevatorId, int floor){
        carButtons[elevatorId-1].add(floor);
        carButtonLabels[elevatorId-1].setText(buildCarString(elevatorId));
    }

    // works
    public void removeCarButtonPressed(int elevatorId, int floor){
        int index = carButtons[elevatorId-1].indexOf(floor);
        carButtons[elevatorId-1].remove(index);
        carButtonLabels[elevatorId-1].setText(buildCarString(elevatorId));
    }

    // works
    public void handleFloorButtonPressed(int floor, String direction){
        floorButtons[floor-1].add(direction);
        floorButtonLabels[floor-1].setText(buildFloorString(floor));
    }

    // works
    public void removeFloorButtonPressed(int floor, String direction){
        int index = floorButtons[floor-1].indexOf(direction);
        floorButtons[floor-1].remove(index);
        floorButtonLabels[floor-1].setText(buildFloorString(floor));
    }

    //works
    public void handleUpdateElevator(int elevatorId, int currFloor, Elevator.ElevatorState status){
        clearColumn(elevatorId-1);
        if(currFloor == 0){
            currFloor = 1;
        }
        elevatorCells[FLOORS - currFloor][elevatorId-1].setText(String.valueOf(status));
        switch(status){
            case IDLE:
                elevatorCells[FLOORS - currFloor][elevatorId - 1].setBorder(BorderFactory.createLineBorder(Color.GRAY));
                break;
            case DOORS_OPEN:
                elevatorCells[FLOORS - currFloor][elevatorId - 1].setBorder(BorderFactory.createLineBorder(Color.BLUE));
                break;
            case MOVING:
                elevatorCells[FLOORS - currFloor][elevatorId - 1].setBorder(BorderFactory.createLineBorder(Color.GREEN));
                break;
            case TRANSIENT_FAULT:
                elevatorCells[FLOORS - currFloor][elevatorId - 1].setBorder(BorderFactory.createLineBorder(Color.YELLOW));
                break;
            case HARD_FAULT:
                elevatorCells[FLOORS - currFloor][elevatorId - 1].setBorder(BorderFactory.createLineBorder(Color.RED));
                break;
            case SHUTDOWN:
                elevatorCells[FLOORS - currFloor][elevatorId - 1].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                break;
        }
    }

    public void handleCompletedTime(Duration time){
        completedTime.setText("Completed Time: " + time.toSeconds());
    }

    public void handleTotalMovements(String movements){
        totalMovements.setText("Total Movements: " + movements);
    }

    public void clearColumn(int column){
        for(int i = 0; i < FLOORS; i++){
            elevatorCells[i][column].setText("");
            elevatorCells[i][column].setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }
    }

    public void initializeArray(ArrayList[] array){
        for(int i = 0; i < array.length; i++){
            array[i] = new ArrayList<>();
        }
    }

    public String buildCarString(int elevatorId){
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < carButtons[elevatorId - 1].size(); i++) {
            if(i > 0){
                str.append(", ");
            }
            str.append(carButtons[elevatorId - 1].get(i));
        }
        return str.toString();
    }

    public String buildFloorString(int floor){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < floorButtons[floor-1].size(); i++){
            if(i > 0){
                str.append(", ");
            }
            str.append(floorButtons[floor-1].get(i));
        }
        return str.toString();
    }



    public static void main(String[] args) {
        GUI gui = new GUI();
        gui.frame.setVisible(true);
    }
}
