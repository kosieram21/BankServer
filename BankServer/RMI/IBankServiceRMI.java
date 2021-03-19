package BankServer.RMI;

import BankServer.Status;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBankServiceRMI extends Remote {
    int createAccount() throws RemoteException, IOException, InterruptedException;
    Status deposit(int uuid, int amount) throws RemoteException, IOException, InterruptedException;
    int getBalance(int uuid) throws RemoteException, IOException, InterruptedException;
    Status transfer(int source_uuid, int target_uuid, int amount) throws RemoteException, IOException, InterruptedException;
}
