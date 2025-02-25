package BankServer;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.io.IOException;

public interface IBankServicePeer extends Remote {
    int createAccount(int timestamp, int server_id) throws IOException, InterruptedException;
    int deposit(int timestamp, int server_id, int uuid, int amount) throws IOException, InterruptedException;
    int getBalance(int timestamp, int server_id, int uuid) throws IOException, InterruptedException;
    int transfer(int timestamp, int server_id, int source_uuid, int target_uuid, int amount) throws IOException, InterruptedException;
    void execute(int timestamp, int server_id) throws IOException;
    void halt() throws IOException, NotBoundException;
}
