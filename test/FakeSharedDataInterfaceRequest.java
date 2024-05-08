import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class FakeSharedDataInterfaceRequest implements SharedDataInterface<Request> {
    private List<Request> requests = new ArrayList<>();

    @Override
    public Request remove() throws RemoteException {
        if (!requests.isEmpty()) {
            return requests.remove(0);
        }
        return null;
    }

    @Override
    public void put(Request message) throws RemoteException {
        requests.add(message);
    }

    @Override
    public Request get(int index) throws RemoteException {
        if (index >= 0 && index < requests.size()) {
            return requests.get(index);
        }
        return null;
    }

    @Override
    public void remove(Request message) throws RemoteException {
        requests.remove(message);
    }

    @Override
    public int size() throws RemoteException {
        return requests.size();
    }
}
