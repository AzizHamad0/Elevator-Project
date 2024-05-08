public class Error {
    public static void handleError(Exception e){
        e.printStackTrace();
        System.exit(1);
    }
}
