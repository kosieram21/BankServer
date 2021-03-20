package BankServer.RMI;

import BankServer.Status;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

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

    @Override
    public int createAccount() throws RemoteException, IOException, InterruptedException, NotBoundException {
        int timestamp = _clock.advance();
        RequestQueue.CreateAccountRequest request = new RequestQueue.CreateAccountRequest(timestamp, _local_server.getServerId());
        RequestQueue.CreateAccountResponse response =
                (RequestQueue.CreateAccountResponse)executeRequest(request,
                        peer -> peer.createAccount(request.getTimestamp(), _local_server.getServerId()));
        return response.getUuid();
    }

    @Override
    public Status deposit(int uuid, int amount) throws RemoteException, IOException, InterruptedException, NotBoundException {
        int timestamp = _clock.advance();
        RequestQueue.DepositRequest request = new RequestQueue.DepositRequest(timestamp, _local_server.getServerId(), uuid, amount);
        RequestQueue.DepositResponse response =
                (RequestQueue.DepositResponse)executeRequest(request,
                        peer -> peer.deposit(request.getTimestamp(), _local_server.getServerId(), uuid, amount));
        return response.getStatus();
    }

    @Override
    public int getBalance(int uuid) throws RemoteException, IOException, InterruptedException, NotBoundException {
        int timestamp = _clock.advance();
        RequestQueue.GetBalanceRequest request = new RequestQueue.GetBalanceRequest(timestamp, _local_server.getServerId(), uuid);
        RequestQueue.GetBalanceResponse response =
                (RequestQueue.GetBalanceResponse)executeRequest(request,
                        peer -> peer.getBalance(request.getTimestamp(), _local_server.getServerId(), uuid));
        return response.getBalance();
    }

    @Override
    public Status transfer(int source_uuid, int target_uuid, int amount) throws RemoteException, IOException, InterruptedException, NotBoundException {
        int timestamp = _clock.advance();
        RequestQueue.TransferRequest request = new RequestQueue.TransferRequest(timestamp, _local_server.getServerId(), source_uuid, target_uuid, amount);
        RequestQueue.TransferResponse response =
                (RequestQueue.TransferResponse)executeRequest(request,
                        peer -> peer.transfer(request.getTimestamp(), _local_server.getServerId(), source_uuid, target_uuid, amount));
        return response.getStatus();
    }

    private RequestQueue.Response executeRequest(RequestQueue.Request request, ThrowingConsumer<IBankServicePeer> peer_message) throws RemoteException, NotBoundException, MalformedURLException, InterruptedException {
        _request_queue.enqueue(request);
        multicast(peer_message);
        RequestQueue.Response response = _request_queue.getResponse(request);
        multicast(peer -> peer.execute(request.getTimestamp(), request.getProcessId()));
        return response;
    }

    private void multicast(ThrowingConsumer<IBankServicePeer> sendMessage) throws RemoteException, NotBoundException, MalformedURLException {
        for (BankServerReplica replica : _peer_servers) {
            IBankServicePeer peer = replica.getBankServicePeerInterface();
            sendMessage.accept(peer);
        }
    }
}
