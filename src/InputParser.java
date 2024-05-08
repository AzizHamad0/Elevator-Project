import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;

public class InputParser {
    public static ArrayList<Request> parseFile(File file) {
        ArrayList<Request> requests = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts.length == 5) {
                    LocalTime time = LocalTime.parse(parts[0]);
                    int sourceFloor = Integer.parseInt(parts[1]);
                    Request.Direction direction = parts[2].equalsIgnoreCase("up") ? Request.Direction.UP : Request.Direction.DOWN;
                    int destinationFloor = Integer.parseInt(parts[3]);
                    int fault = Integer.parseInt(parts[4]);

                    Request request = new Request(time, sourceFloor, direction, destinationFloor, fault, false);
                    requests.add(request);
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            System.exit(1);
        }
        requests.sort(Comparator.comparing(Request::getTime));
        requests.get(requests.size()-1).setIsLastRequest(true);
        return requests;
    }
}
