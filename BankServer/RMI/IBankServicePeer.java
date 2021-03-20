package BankServer.RMI;

import java.rmi.Remote;
import java.io.IOException;
import java.rmi.RemoteException;

public interface IBankServicePeer extends Remote {
    int createAccount(int timestamp, int pId) throws RemoteException, IOException, InterruptedException;
    int deposit(int timestamp, int pId, int uuid, int amount) throws RemoteException, IOException, InterruptedException;
    int getBalance(int timestamp, int pId, int uuid) throws RemoteException, IOException, InterruptedException;
    int transfer(int timestamp, int pId, int source_uuid, int target_uuid, int amount) throws RemoteException, IOException, InterruptedException;
    void execute(int timestamp, int pId) throws RemoteException;
}
