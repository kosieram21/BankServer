package BankServer.RMI;

import BankServer.Bank;
import BankServer.Status;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.util.EventObject;
import java.util.PriorityQueue;

public class RequestQueue {
    // region Response
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

    private final PriorityQueue<Request> _queue;
    private final EventListenerList _eventListeners;

    RequestQueue() {
        _queue = new PriorityQueue<Request>();
        _eventListeners = new EventListenerList();
    }

    public Response getResponse(Request request) throws InterruptedException {
        Request front = _queue.peek();
        if(front.getTimestamp() != request.getTimestamp() && front.getProcessId() != request.getProcessId()) {
            request.wait();
        }
        return request.execute();
    }

    public void processRequests() throws InterruptedException {
        // This method should be running in a background thread to process
        // request in the queue according to lamport's method This method should also
        // fire the event processed event to alert the server's client interface of the
        // proper response to send back to the client. An optimization that could be made
        // is to only have the background thread run when there are events in the queue.

        Request request = _queue.poll();
        RequestProcessedEvent event = request.execute();

        raiseRequestProcessedEvent(event);
    }

    public synchronized void enqueue(Request request) {
        _queue.add(request);
    }

    public void addRequestProcessedListener(IRequestProcessedListener listener) {
        _eventListeners.add(IRequestProcessedListener.class, listener);
    }

    public void removeEventProcessedListener(IRequestProcessedListener listener) {
        _eventListeners.remove(IRequestProcessedListener.class, listener);
    }

    private void raiseRequestProcessedEvent(RequestQueue.RequestProcessedEvent event) throws InterruptedException {
        IRequestProcessedListener[] listeners = _eventListeners.getListeners(IRequestProcessedListener.class);
        for(IRequestProcessedListener listener : listeners) {
            if (event instanceof CreateAccountProcessedEvent)
                listener.createAccountProcessed((CreateAccountProcessedEvent) event);
            else if (event instanceof DepositProcessedEvent)
                listener.depositProcessed((DepositProcessedEvent) event);
            else if (event instanceof GetBalanceProcessedEvent)
                listener.getBalanceProcessed((GetBalanceProcessedEvent) event);
            else if (event instanceof TransferProcessedEvent)
                listener.transferProcessed((TransferProcessedEvent) event);
        }
    }

    private static RequestQueue _instance;
    public synchronized static RequestQueue getInstance() {
        if (_instance == null)
            _instance = new RequestQueue();
        return _instance;
    }
}
