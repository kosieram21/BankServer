package BankServer.RMI;

import BankServer.Bank;

import java.io.IOException;

public class BankServicePeer implements IBankServicePeer {
    private static final StateMachine.Request.Source REQUEST_SOURCE = StateMachine.Request.Source.Server;

    private final LamportClock _clock;
    private final StateMachine _state_machine;

    private final int _local_server_id;

    public BankServicePeer(int local_server_id) {
        super();
        _clock = LamportClock.getInstance();
        _state_machine = StateMachine.getInstance();

        _local_server_id = local_server_id;
    }

    @Override
    public int createAccount(int timestamp, int server_id) throws IOException, InterruptedException {
        return enqueueRequest(new StateMachine.CreateAccountRequest(REQUEST_SOURCE, timestamp, server_id));
    }

    @Override
    public int deposit(int timestamp, int server_id, int uuid, int amount) throws IOException, InterruptedException {
        return enqueueRequest(new StateMachine.DepositRequest(REQUEST_SOURCE, timestamp, server_id, uuid, amount));
    }

    @Override
    public int getBalance(int timestamp, int server_id, int uuid) throws IOException, InterruptedException {
        return enqueueRequest(new StateMachine.GetBalanceRequest(REQUEST_SOURCE, timestamp, server_id, uuid));
    }

    @Override
    public int transfer(int timestamp, int server_id, int source_uuid, int target_uuid, int amount) throws IOException, InterruptedException {
        return enqueueRequest(new StateMachine.TransferRequest(REQUEST_SOURCE, timestamp, server_id, source_uuid, target_uuid, amount));
    }

    private int enqueueRequest(StateMachine.Request request) {
        int timestamp = _clock.merge(request.getTimestamp());
        _state_machine.enqueue(request);
        return timestamp;
    }

    @Override
    public void execute(int timestamp, int server_id) throws IOException {
        _state_machine.executeImmediately(timestamp, server_id, _local_server_id);
    }

    @Override
    public void halt() {
        // TODO: shutdown server
    }
}
