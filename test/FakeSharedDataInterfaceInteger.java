import java.rmi.RemoteException;

public class FakeSharedDataInterfaceInteger implements SharedDataInterface<Integer> {
    private Integer message = null;

    @Override
    public Integer remove() {
        Integer temp = message;
        message = null;
        return temp;
    }

    @Override
    public void put(Integer message) {
        this.message = message;
    }

    @Override
    public Integer get(int index) throws RemoteException {
        return null;
    }

    @Override
    public void remove(Integer message) throws RemoteException {

    }

    @Override
    public int size() throws RemoteException {
        return 0;
    }


}
