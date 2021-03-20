package BankServer.RMI;

import BankServer.Status;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.List;

public class BankService implements IBankService {
    private  final LamportClock _clock;
    private final RequestQueue _request_queue;

    private final BankServerReplica _local_server;
    private final List<BankServerReplica> _peer_servers;

    public BankService(BankServerReplica local_server, List<BankServerReplica> peer_servers) {
        super();
        _clock = LamportClock.getInstance();
        _request_queue = RequestQueue.getInstance();

        _local_server = local_server;
        _peer_servers = peer_servers;
    }

    @Override
    public int createAccount() throws IOException, InterruptedException, NotBoundException {
        int timestamp = _clock.advance();
        RequestQueue.CreateAccountRequest request = new RequestQueue.CreateAccountRequest(timestamp, _local_server.getServerId());
        RequestQueue.CreateAccountResponse response = (RequestQueue.CreateAccountResponse)executeRequest(request);
        return response.getUuid();
    }

    @Override
    public Status deposit(int uuid, int amount) throws IOException, InterruptedException, NotBoundException {
        int timestamp = _clock.advance();
        RequestQueue.DepositRequest request = new RequestQueue.DepositRequest(timestamp, _local_server.getServerId(), uuid, amount);
        RequestQueue.DepositResponse response = (RequestQueue.DepositResponse)executeRequest(request);
        return response.getStatus();
    }

    @Override
    public int getBalance(int uuid) throws IOException, InterruptedException, NotBoundException {
        int timestamp = _clock.advance();
        RequestQueue.GetBalanceRequest request = new RequestQueue.GetBalanceRequest(timestamp, _local_server.getServerId(), uuid);
        RequestQueue.GetBalanceResponse response = (RequestQueue.GetBalanceResponse)executeRequest(request);
        return response.getBalance();
    }

    @Override
    public Status transfer(int source_uuid, int target_uuid, int amount)
            throws IOException, InterruptedException, NotBoundException
    {
        int timestamp = _clock.advance();
        RequestQueue.TransferRequest request = new RequestQueue.TransferRequest(timestamp, _local_server.getServerId(), source_uuid, target_uuid, amount);
        RequestQueue.TransferResponse response = (RequestQueue.TransferResponse)executeRequest(request);
        return response.getStatus();
    }

    private RequestQueue.Response executeRequest(RequestQueue.Request request)
            throws IOException, NotBoundException, InterruptedException
    {
        _request_queue.enqueue(request);
        multicast(request);
        RequestQueue.Response response = _request_queue.execute(request);
        multicast(request.getTimestamp(), request.getProcessId());
        return response;
    }

    private void multicast(RequestQueue.Request request) throws InterruptedException, IOException, NotBoundException {
        int max_timestamp = 0;
        for (BankServerReplica peer : _peer_servers) {
            int timestamp = peer.receiveRequest(request);
            max_timestamp = Math.max(max_timestamp, timestamp);
        }
        _clock.merge(max_timestamp);
    }

    private void multicast(int timestamp, int pId) throws InterruptedException, IOException, NotBoundException {
        for (BankServerReplica peer : _peer_servers)
            peer.executeRequest(timestamp, pId);
    }
}
