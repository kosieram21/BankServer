package BankServer.RMI;

import java.io.IOException;

public class BankServicePeer implements IBankServicePeer {
    private final LamportClock _clock;
    private final RequestQueue _request_queue;

    private final BankServerReplica _local_server;

    public BankServicePeer(BankServerReplica local_server) {
        super();
        _clock = LamportClock.getInstance();
        _request_queue = RequestQueue.getInstance();

        _local_server = local_server;
    }

    @Override
    public int createAccount(int timestamp, int pId) throws IOException, InterruptedException {
        return enqueueRequest(new RequestQueue.CreateAccountRequest(timestamp, pId));
    }

    @Override
    public int deposit(int timestamp, int pId, int uuid, int amount) throws IOException, InterruptedException {
        return enqueueRequest(new RequestQueue.DepositRequest(timestamp, pId, uuid, amount));
    }

    @Override
    public int getBalance(int timestamp, int pId, int uuid) throws IOException, InterruptedException {
        return enqueueRequest(new RequestQueue.GetBalanceRequest(timestamp, pId, uuid));
    }

    @Override
    public int transfer(int timestamp, int pId, int source_uuid, int target_uuid, int amount) throws IOException, InterruptedException {
        return enqueueRequest(new RequestQueue.TransferRequest(timestamp, pId, source_uuid, target_uuid, amount));
    }

    private int enqueueRequest(RequestQueue.Request request) {
        int timestamp = _clock.merge(request.getTimestamp());
        _request_queue.enqueue(request);
        return timestamp;
    }

    @Override
    public void execute(int timestamp, int pId) throws IOException {
        _request_queue.executeImmediately(timestamp, pId, _local_server.getServerId());
    }
}
