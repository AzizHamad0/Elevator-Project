public class RequestPickedUpPair {
    private Request request;
    private boolean pickedUp;

    public RequestPickedUpPair(Request request, boolean pickedUp) {
        this.request = request;
        this.pickedUp = pickedUp;
    }

    public Request getRequest() {
        return request;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public void setPickedUp(boolean pickedUp) {
        this.pickedUp = pickedUp;
    }
}
