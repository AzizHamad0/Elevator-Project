import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class SharedDataInterfaceStub<T> implements SharedDataInterface<T> {
    private List<T> items = new ArrayList<>();

    @Override
    public T remove() throws RemoteException {
        if (!items.isEmpty()) {
            return items.remove(0);
        }
        return null;
    }

    @Override
    public void put(T item) throws RemoteException {
        items.add(item);
    }

    @Override
    public T get(int index) throws RemoteException {
        if (index >= 0 && index < items.size()) {
            return items.get(index);
        }
        return null;
    }

    @Override
    public void remove(T item) throws RemoteException {
        items.remove(item);
    }

    @Override
    public int size() throws RemoteException {
        return items.size();
    }


    public void setItems(List<T> newItems) {
        this.items = new ArrayList<>(newItems);
    }

}
