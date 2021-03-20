package BankServer.RMI;

import java.io.IOException;
import java.rmi.RemoteException;

public class BankServicePeer implements IBankServicePeer{
    private final LamportClock _clock = LamportClock.getInstance();
    private final RequestQueue _request_queue = RequestQueue.getInstance();

    @Override
    public int createAccount(int timestamp, int pId) throws RemoteException, IOException, InterruptedException {
        _clock.merge(timestamp);
        _clock.advance();
        _request_queue.enqueue(new RequestQueue.CreateAccountRequest(timestamp, pId));
        return _clock.getValue();
    }

    @Override
    public int deposit(int timestamp, int pId, int uuid, int amount) throws RemoteException, IOException, InterruptedException {
        _clock.merge(timestamp);
        _clock.advance();
        _request_queue.enqueue(new RequestQueue.DepositRequest(timestamp, pId, uuid, amount));
        return _clock.getValue();
    }

    @Override
    public int getBalance(int timestamp, int pId, int uuid) throws RemoteException, IOException, InterruptedException {
        _clock.merge(timestamp);
        _clock.advance();
        _request_queue.enqueue(new RequestQueue.GetBalanceRequest(timestamp, pId, uuid));
        return _clock.getValue();
    }

    @Override
    public int transfer(int timestamp, int pId, int source_uuid, int target_uuid, int amount) throws RemoteException, IOException, InterruptedException {
        _clock.merge(timestamp);
        _clock.advance();
        _request_queue.enqueue(new RequestQueue.TransferRequest(timestamp, pId, source_uuid, target_uuid, amount));
        return _clock.getValue();
    }

    @Override
    public void execute(int timestamp, int pId) throws RemoteException {

    }
}
