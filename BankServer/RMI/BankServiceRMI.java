package BankServer.RMI;

import BankServer.Bank;
import BankServer.Status;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

public class BankServiceRMI implements IBankServiceRMI {
    private final Bank _bank;
    private  final LamportClock _clock;
    private final RequestQueue _request_queue;
    private final BankServerReplica _local_server;
    private final List<BankServerReplica> _peer_servers;

    public BankServiceRMI (BankServerReplica local_server, List<BankServerReplica> peer_servers) throws RemoteException, IOException {
        super();
        _bank = Bank.getInstance();
        _clock = LamportClock.getInstance();
        _request_queue = RequestQueue.getInstance();
        _local_server = local_server;
        _peer_servers = peer_servers;
    }

    @Override
    public int createAccount() throws RemoteException {
        _clock.advance();
        _request_queue.enqueue(new RequestQueue.CreateAccountRequest(_clock.getValue(), _local_server.getServerId()));
        // TODO: multicast to peer servers
        return _bank.createAccount(); // TODO: get response from event
    }

    @Override
    public Status deposit(int uuid, int amount) throws RemoteException {
        _clock.advance();
        _request_queue.enqueue(new RequestQueue.DepositRequest(_clock.getValue(), _local_server.getServerId(), uuid, amount));
        // TODO: multicast to peer servers
        return _bank.deposit(uuid, amount); // TODO: get response from event
    }

    @Override
    public int getBalance(int uuid) throws RemoteException {
        _clock.advance();
        _request_queue.enqueue(new RequestQueue.GetBalanceRequest(_clock.getValue(), _local_server.getServerId(), uuid));
        // TODO: multicast to peer servers
        return _bank.getBalance(uuid); // TODO: get response from event
    }

    @Override
    public Status transfer(int source_uuid, int target_uuid, int amount) throws RemoteException {
        _clock.advance();
        _request_queue.enqueue(new RequestQueue.TransferRequest(_clock.getValue(), _local_server.getServerId(), source_uuid, target_uuid, amount));
        // TODO: multicast to peer servers
        return _bank.transfer(source_uuid, target_uuid, amount); // TODO: get response from event
    }
}
