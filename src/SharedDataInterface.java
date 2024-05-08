import java.rmi.Remote;
import java.rmi.RemoteException;
public interface SharedDataInterface<T> extends Remote{
    T remove() throws RemoteException;
    void put(T message) throws RemoteException;
    T get(int index) throws RemoteException;
    void remove(T message) throws RemoteException;
    int size() throws RemoteException;
}