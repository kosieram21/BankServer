import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBankServiceRMI extends Remote {
    int createAccount() throws RemoteException;
    Status deposit(int uuid, int amount) throws RemoteException;
    int getBalance(int uuid) throws RemoteException;
    Status transfer(int source_uuid, int target_uuid, int amount) throws RemoteException;
}
