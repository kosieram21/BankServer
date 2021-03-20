package BankServer.RMI;

import BankServer.Bank;
import BankServer.Status;

import java.io.IOException;
import java.util.PriorityQueue;

public class RequestQueue {
    // region Server-to-Client Response
    static abstract class Response { }

    static class CreateAccountResponse extends Response {
        private final int _uuid;

        public CreateAccountResponse(int uuid) {
            _uuid = uuid;
        }

        public int getUuid() {
            return _uuid;
        }
    }

    static class DepositResponse extends Response {
        private final Status _status;

        public DepositResponse(Status status) {
            _status = status;
        }

        public Status getStatus() {
            return _status;
        }
    }

    static class GetBalanceResponse extends Response {
        private final int _balance;

        public GetBalanceResponse(int balance) {
            _balance = balance;
        }

        public int getBalance() {
            return _balance;
        }
    }

    static class TransferResponse extends Response {
        private final Status _status;

        public TransferResponse(Status status) {
            _status = status;
        }

        public Status getStatus() {
            return _status;
        }
    }

    // endregion

    // region Request Hierarchy

    static abstract class Request implements Comparable<Request> {
        private final int _timestamp;
        private final int _processId;

        Request(int timestamp, int processId) {
            _timestamp = timestamp;
            _processId = processId;
        }

        public int getTimestamp() { return _timestamp; }

        public int getProcessId() { return _processId; }

        @Override
        public int compareTo(Request o) {
            return getTimestamp() == o.getTimestamp() ?
                    Integer.compare(getProcessId(), o.getProcessId()) :
                    Integer.compare(getTimestamp(), o.getTimestamp());
        }

        abstract Response execute() throws IOException;

        abstract int sendToPeer(IBankServicePeer peer) throws IOException, InterruptedException;
    }

    static class CreateAccountRequest extends Request {
        CreateAccountRequest(int timestamp, int processId) throws IOException {
            super(timestamp, processId);
        }

        CreateAccountResponse execute() throws IOException {
            Bank bank = Bank.getInstance();
            int uuid = bank.createAccount();
            return new CreateAccountResponse(uuid);
        }

        int sendToPeer(IBankServicePeer peer) throws IOException, InterruptedException {
            return peer.createAccount(getTimestamp(), getProcessId());
        }
    }

    static class DepositRequest extends Request {
        private final int _uuid;
        private final int _amount;

        DepositRequest(int timestamp, int processId, int uuid, int amount) throws IOException {
            super(timestamp, processId);
            _uuid = uuid;
            _amount = amount;
        }

        public int getUuid() {
            return _uuid;
        }

        public int getAmount() {
            return _amount;
        }

        DepositResponse execute() throws IOException {
            Bank bank = Bank.getInstance();
            Status status = bank.deposit(_uuid, _amount);
            return new DepositResponse(status);
        }

        int sendToPeer(IBankServicePeer peer) throws IOException, InterruptedException {
            return peer.deposit(getTimestamp(), getProcessId(), getUuid(), getAmount());
        }
    }

    static class GetBalanceRequest extends Request {
        private final int _uuid;

        GetBalanceRequest(int timestamp, int processId, int uuid) throws IOException {
            super(timestamp, processId);
            _uuid = uuid;
        }

        public int getUuid() {
            return _uuid;
        }

        GetBalanceResponse execute() throws IOException {
            Bank bank = Bank.getInstance();
            int balance = bank.getBalance(_uuid);
            return new GetBalanceResponse(balance);
        }

        int sendToPeer(IBankServicePeer peer) throws IOException, InterruptedException {
            return peer.getBalance(getTimestamp(), getProcessId(), getUuid());
        }
    }

    static class TransferRequest extends Request {
        private final int _sourceUuid;
        private final int _targetUuid;
        private final int _amount;

        TransferRequest(int timestamp, int processId, int sourceUuid, int targetUuid, int amount) throws IOException {
            super(timestamp, processId);
            _sourceUuid = sourceUuid;
            _targetUuid = targetUuid;
            _amount = amount;
        }

        public int getSourceUuid() {
            return _sourceUuid;
        }

        public int getTargetUuid() {
            return _targetUuid;
        }

        public int getAmount() {
            return _amount;
        }

        TransferResponse execute() throws IOException {
            Bank bank = Bank.getInstance();
            Status status = bank.transfer(_sourceUuid, _targetUuid, _amount);
            return new TransferResponse(status);
        }

        int sendToPeer(IBankServicePeer peer) throws IOException, InterruptedException {
            return peer.transfer(getTimestamp(), getProcessId(), getSourceUuid(), getTargetUuid(), getAmount());
        }
    }

    // endregion

    private final PriorityQueue<Request> _queue = new PriorityQueue<Request>();

    public synchronized void enqueue(Request request) {
        _queue.add(request);
    }

    public Response execute(Request request) throws InterruptedException, IOException {
        Request front = _queue.peek();
        if (front != null) {
            if (request.compareTo(front) != 0)
                request.wait();

            front = _queue.poll();
            return front.execute();
        }
        else throw new NullPointerException("Response lost and thus cannot be returned");
    }

    public synchronized Response executeImmediately(int timestamp, int pId, int local_pId) throws NullPointerException, IOException {
        Request matching_request = null;
        for (Request request : _queue) {
            if (request.getTimestamp() == timestamp && request.getProcessId() == pId) {
                matching_request = request;
                break;
            }
        }

        if (matching_request == null) throw new NullPointerException("No matching request to remove");
        _queue.remove(matching_request);
        Response matching_response = matching_request.execute();

        Request next_request = _queue.peek();
        if (next_request != null && next_request.getProcessId() == local_pId)
            next_request.notify();

        return matching_response;
    }

    private static RequestQueue _instance;
    public synchronized static RequestQueue getInstance() {
        if (_instance == null)
            _instance = new RequestQueue();
        return _instance;
    }
}
