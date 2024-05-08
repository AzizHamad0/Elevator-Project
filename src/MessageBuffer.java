import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;

public class MessageBuffer<T> extends UnicastRemoteObject implements SharedDataInterface<T> {
    private final ArrayList<T> messages;

    /**
     * Constructor initializes the buffer.
     */
    public MessageBuffer() throws RemoteException {
        messages = new ArrayList<>();
    }
    /**
     * Retrieves and removes the oldest request from the buffer, waiting if no requests are available.
     * @return The oldest request in the buffer.
     */
    @Override
    public synchronized T remove() throws RemoteException {
        if(messages.isEmpty()){
            return null;
        }
        return messages.remove(0);
    }

    /**
     * Adds a new request to the buffer and notifies waiting threads.
     * @param message The request to be added to the buffer.
     */
    @Override
    public synchronized void put(T message) throws RemoteException{
        messages.add(message);
    }

    @Override
    public synchronized T get(int index) throws RemoteException {
        if (index < 0 || index >= messages.size()) {
            return null;
        }
        return messages.get(index);
    }
    @Override
    public synchronized void remove(T message) throws RemoteException {
        Iterator<T> iterator = messages.iterator();
        while (iterator.hasNext()) {
            T msg = iterator.next();
            if (msg.equals(message)) {
                iterator.remove();
                return; // Assuming we only want to remove the first occurrence
            }
        }
    }
    @Override
    public synchronized int size(){
        return messages.size();
    }
}