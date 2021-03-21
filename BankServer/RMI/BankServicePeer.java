package BankServer.RMI;

import java.io.IOException;

public class BankServicePeer implements IBankServicePeer {
    private final LamportClock _clock;
    private final RequestQueue _request_queue;

    private final int _local_server_id;

    public BankServicePeer(int local_server_id) {
        super();
        _clock = LamportClock.getInstance();
        _request_queue = RequestQueue.getInstance();

        _local_server_id = local_server_id;
    }

    @Override
    public int createAccount(int timestamp, int server_id) throws IOException, InterruptedException {
        return enqueueRequest(new RequestQueue.CreateAccountRequest(timestamp, server_id));
    }

    @Override
    public int deposit(int timestamp, int server_id, int uuid, int amount) throws IOException, InterruptedException {
        return enqueueRequest(new RequestQueue.DepositRequest(timestamp, server_id, uuid, amount));
    }

    @Override
    public int getBalance(int timestamp, int server_id, int uuid) throws IOException, InterruptedException {
        return enqueueRequest(new RequestQueue.GetBalanceRequest(timestamp, server_id, uuid));
    }

    @Override
    public int transfer(int timestamp, int server_id, int source_uuid, int target_uuid, int amount) throws IOException, InterruptedException {
        return enqueueRequest(new RequestQueue.TransferRequest(timestamp, server_id, source_uuid, target_uuid, amount));
    }

    private int enqueueRequest(RequestQueue.Request request) {
        int timestamp = _clock.merge(request.getTimestamp());
        _request_queue.enqueue(request);
        return timestamp;
    }

    @Override
    public void execute(int timestamp, int server_id) throws IOException {
        _request_queue.executeImmediately(timestamp, server_id, _local_server_id);
    }

    @Override
    public void halt() {
        // shutdown server
    }
}
