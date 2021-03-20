package BankServer.RMI;

import BankServer.Bank;
import BankServer.Status;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.util.EventObject;
import java.util.Iterator;
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
        protected final Bank _bank;

        Request(int timestamp, int processId) throws IOException {
            _timestamp = timestamp;
            _processId = processId;
            _bank = Bank.getInstance();
        }

        public int getTimestamp() { return _timestamp; }

        public int getProcessId() { return _processId; }

        @Override
        public int compareTo(Request o) {
            return Integer.compare(_timestamp, o._timestamp) == 0 ?
                    Integer.compare(_processId, o._processId) :
                    Integer.compare(_timestamp, o._timestamp);
        }

        abstract Response execute();
    }

    static class CreateAccountRequest extends Request {
        CreateAccountRequest(int timestamp, int processId) throws IOException {
            super(timestamp, processId);
        }

        CreateAccountResponse execute() {
            int uuid = _bank.createAccount();
            return new CreateAccountResponse(uuid);
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

        DepositResponse execute() {
            Status status = _bank.deposit(_uuid, _amount);
            return new DepositResponse(status);
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

        GetBalanceResponse execute() {
            int balance = _bank.getBalance(_uuid);
            return new GetBalanceResponse(balance);
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

        TransferResponse execute() {
            Status status = _bank.transfer(_sourceUuid, _targetUuid, _amount);
            return new TransferResponse(status);
        }
    }

    // endregion

    private final PriorityQueue<Request> _queue = new PriorityQueue<Request>();

    public Response getResponse(Request request) throws InterruptedException {
        Request front = _queue.peek();
        if (front != null) {
            if (request.compareTo(front) != 0)
                request.wait();

            front = _queue.poll();
            return front.execute();
        }
        else throw new NullPointerException("Response lost and thus cannot be returned");
    }

    public synchronized Request remove(int timestamp, int pId) throws NullPointerException {
        Request matching_request = null;
        for (Request request : _queue) {
            if (request.getTimestamp() == timestamp && request.getProcessId() == pId) {
                matching_request = request;
                break;
            }
        }

        if (matching_request == null) throw new NullPointerException("No matching request to remove");
        _queue.remove(matching_request);

        Request next_request = _queue.peek();
        if (next_request != null && next_request.getProcessId() == pId)
            next_request.notify();

        return matching_request;
    }

    public synchronized void enqueue(Request request) {
        _queue.add(request);
    }

    private static RequestQueue _instance;
    public synchronized static RequestQueue getInstance() {
        if (_instance == null)
            _instance = new RequestQueue();
        return _instance;
    }
}
