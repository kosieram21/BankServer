package BankServer.RMI;

import BankServer.Status;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

public class BankServiceRMI implements IBankServiceRMI, IRequestProcessedListener {
    static class Response<TValue> {
        private int _timestamp;
        private TValue _value;

        public int getTimestamp() {
            return _timestamp;
        }

        public void setTimestamp(int timestamp) {
            _timestamp = timestamp;
        }

        public TValue getValue() {
            return _value;
        }

        public void setValue(TValue value) {
            _value = value;
        }
    }

    private  final LamportClock _clock;
    private final RequestQueue _request_queue;

    private final BankServerReplica _local_server;
    private final List<BankServerReplica> _peer_servers;

    private final Response<Integer> _create_account_response;
    private final Response<Status> _deposit_response;
    private final Response<Integer> _get_balance_response;
    private final Response<Status> _transfer_response;

    public BankServiceRMI (BankServerReplica local_server, List<BankServerReplica> peer_servers)
            throws RemoteException, IOException, InterruptedException {
        super();
        _clock = LamportClock.getInstance();
        _request_queue = RequestQueue.getInstance();
        _request_queue.addRequestProcessedListener(this);

        _local_server = local_server;
        _peer_servers = peer_servers;

        _create_account_response = new Response<Integer>();
        _deposit_response = new Response<Status>();
        _get_balance_response = new Response<Integer>();
        _transfer_response = new Response<Status>();
    }

    @Override
    public int createAccount() throws RemoteException, IOException, InterruptedException, NotBoundException {
        _clock.advance();
        _create_account_response.setTimestamp(_clock.getValue());
        _request_queue.enqueue(new RequestQueue.CreateAccountRequest(_clock.getValue(), _local_server.getServerId()));

        // multicast to peer servers
        for (BankServerReplica replica : _peer_servers) {
            IBankServicePeer peer = replica.getBankServicePeerInterface();
            peer.createAccount(_clock.getValue(), _local_server.getServerId());
        }

        _create_account_response.wait();
        return _create_account_response.getValue();
    }

    @Override
    public void createAccountProcessed(RequestQueue.CreateAccountProcessedEvent event) throws InterruptedException {
        if(event.getTimestamp() == _create_account_response.getTimestamp() &&
           event.getProcessId() == _local_server.getServerId()) {
            _create_account_response.setValue(event.getUuid());
            _create_account_response.notify();
        }
    }

    @Override
    public Status deposit(int uuid, int amount) throws RemoteException, IOException, InterruptedException, NotBoundException {
        _clock.advance();
        _deposit_response.setTimestamp(_clock.getValue());
        _request_queue.enqueue(new RequestQueue.DepositRequest(_clock.getValue(), _local_server.getServerId(), uuid, amount));

        // multicast to peer servers
        for (BankServerReplica replica : _peer_servers) {
            IBankServicePeer peer = replica.getBankServicePeerInterface();
            peer.deposit(_clock.getValue(), _local_server.getServerId(), uuid, amount);
        }

        _deposit_response.wait();
        return _deposit_response.getValue();
    }

    @Override
    public void depositProcessed(RequestQueue.DepositProcessedEvent event) throws InterruptedException {
        if(event.getTimestamp() == _deposit_response.getTimestamp() &&
                event.getProcessId() == _local_server.getServerId()) {
            _deposit_response.setValue(event.getStatus());
            _deposit_response.notify();
        }
    }

    @Override
    public int getBalance(int uuid) throws RemoteException, IOException, InterruptedException, NotBoundException {
        _clock.advance();
        _get_balance_response.setTimestamp(_clock.getValue());
        _request_queue.enqueue(new RequestQueue.GetBalanceRequest(_clock.getValue(), _local_server.getServerId(), uuid));

        // multicast to peer servers
        for (BankServerReplica replica : _peer_servers) {
            IBankServicePeer peer = replica.getBankServicePeerInterface();
            peer.getBalance(_clock.getValue(), _local_server.getServerId(), uuid);
        }

        _get_balance_response.wait();
        return _get_balance_response.getValue();
    }

    @Override
    public void getBalanceProcessed(RequestQueue.GetBalanceProcessedEvent event) throws InterruptedException {
        if(event.getTimestamp() == _get_balance_response.getTimestamp() &&
                event.getProcessId() == _local_server.getServerId()) {
            _get_balance_response.setValue(event.getBalance());
            _get_balance_response.notify();
        }
    }

    @Override
    public Status transfer(int source_uuid, int target_uuid, int amount) throws RemoteException, IOException, InterruptedException, NotBoundException {
        _clock.advance();
        _transfer_response.setTimestamp(_clock.getValue());
        _request_queue.enqueue(new RequestQueue.TransferRequest(_clock.getValue(), _local_server.getServerId(), source_uuid, target_uuid, amount));

        // multicast to peer servers
        for (BankServerReplica replica : _peer_servers) {
            IBankServicePeer peer = replica.getBankServicePeerInterface();
            peer.transfer(_clock.getValue(), _local_server.getServerId(), source_uuid, target_uuid, amount);
        }

        _transfer_response.wait();
        return _transfer_response.getValue();
    }

    @Override
    public void transferProcessed(RequestQueue.TransferProcessedEvent event) throws InterruptedException {
        if(event.getTimestamp() == _transfer_response.getTimestamp() &&
                event.getProcessId() == _local_server.getServerId()) {
            _transfer_response.setValue(event.getStatus());
            _transfer_response.notify();
        }
    }
}
