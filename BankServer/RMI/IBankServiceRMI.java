package BankServer.RMI;

import BankServer.Status;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.io.IOException;
import java.rmi.RemoteException;

public interface IBankServiceRMI extends Remote {
    int createAccount() throws RemoteException, IOException, InterruptedException, NotBoundException;
    Status deposit(int uuid, int amount) throws RemoteException, IOException, InterruptedException, NotBoundException;
    int getBalance(int uuid) throws RemoteException, IOException, InterruptedException, NotBoundException;
    Status transfer(int source_uuid, int target_uuid, int amount) throws RemoteException, IOException, InterruptedException, NotBoundException;
}
