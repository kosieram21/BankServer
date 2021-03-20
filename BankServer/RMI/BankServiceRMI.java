package BankServer.RMI;

import BankServer.Status;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.function.Consumer;

public class BankServiceRMI implements IBankServiceRMI {
    private  final LamportClock _clock;
    private final RequestQueue _request_queue;

    private final BankServerReplica _local_server;
    private final List<BankServerReplica> _peer_servers;

    public BankServiceRMI (BankServerReplica local_server, List<BankServerReplica> peer_servers)
            throws RemoteException, IOException, InterruptedException {
        super();
        _clock = LamportClock.getInstance();
        _request_queue = RequestQueue.getInstance();

        _local_server = local_server;
        _peer_servers = peer_servers;
    }

    private void multicast(Consumer<IBankServicePeer> sendMessage) throws RemoteException, NotBoundException, MalformedURLException {
        for (BankServerReplica replica : _peer_servers) {
            IBankServicePeer peer = replica.getBankServicePeerInterface();
            sendMessage.accept(peer);
        }
    }

    @Override
    public int createAccount() throws RemoteException, IOException, InterruptedException, NotBoundException {
        _clock.advance();

        RequestQueue.CreateAccountRequest request = new RequestQueue.CreateAccountRequest(_clock.getValue(), _local_server.getServerId());
        _request_queue.enqueue(request);

        // multicast to peer servers
        multicast(peer -> peer.createAccount(_clock.getValue(), _local_server.getServerId()));
        for (BankServerReplica replica : _peer_servers) {
            IBankServicePeer peer = replica.getBankServicePeerInterface();
            peer.createAccount(_clock.getValue(), _local_server.getServerId());
        }

        RequestQueue.CreateAccountResponse response = (RequestQueue.CreateAccountResponse)_request_queue.getResponse(request);

        // multicast execute
        for (BankServerReplica replica : _peer_servers) {
            IBankServicePeer peer = replica.getBankServicePeerInterface();
            peer.execute(request.getTimestamp(), request.getProcessId());
        }

        return response.getUuid();
    }

    @Override
    public Status deposit(int uuid, int amount) throws RemoteException, IOException, InterruptedException, NotBoundException {
        _clock.advance();
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
    public int getBalance(int uuid) throws RemoteException, IOException, InterruptedException, NotBoundException {
        _clock.advance();
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
    public Status transfer(int source_uuid, int target_uuid, int amount) throws RemoteException, IOException, InterruptedException, NotBoundException {
        _clock.advance();
        _request_queue.enqueue(new RequestQueue.TransferRequest(_clock.getValue(), _local_server.getServerId(), source_uuid, target_uuid, amount));

        // multicast to peer servers
        for (BankServerReplica replica : _peer_servers) {
            IBankServicePeer peer = replica.getBankServicePeerInterface();
            peer.transfer(_clock.getValue(), _local_server.getServerId(), source_uuid, target_uuid, amount);
        }

        _transfer_response.wait();
        return _transfer_response.getValue();
    }
}
