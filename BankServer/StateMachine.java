package BankServer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.PriorityQueue;

public class StateMachine {
    private static final LogFile.Server _logger = LogFile.Server.getInstance();

    // region Server-to-Client Response
    static class Response { }

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
        enum Source { Client, Server }

        private final Source _request_source;
        private final int _timestamp;
        private final int _server_id;

        Request(Source request_source, int timestamp, int server_id) {
            _request_source = request_source;
            _timestamp = timestamp;
            _server_id = server_id;
        }

        //region Log Message formation
        private String getPhysicalTimestamp() {
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
            Date now = new Date();
            return sdfDate.format(now);
        }

        protected String getRequestName() { return String.format("%s", getClass().getName()); }

        private String generateLogPrefix(String log_event) {
            // Target Log Message:
            // Server-ID [ CLIENT-REQ ~OR~ SRV-REQ ] Physical-clock-time Request-Timestamp Operation-name Parameters
            //----------
            // `Operation-name` & `Parameters` handled in subclasses
            return String.format("%d %s %s [%d,%d]",
                    getServerId(), log_event, getPhysicalTimestamp(),
                    getTimestamp(), getServerId());
        }

        abstract String generateLogSuffix();

        public String getExecuteLogMessage() { return String.format("%s", generateLogPrefix("REQ_PROCESSING")); }


        public String getEnqueueLogMessage() { return String.format("%s %s", generateLogPrefix(String.format("%s", getRequestSource())), generateLogSuffix()); }
        //endregion

        public Source getRequestSource() { return _request_source;}

        public int getTimestamp() { return _timestamp; }

        public int getServerId() { return _server_id; }

        @Override
        public int compareTo(Request o) {
            return getTimestamp() == o.getTimestamp() ?
                    Integer.compare(getServerId(), o.getServerId()) :
                    Integer.compare(getTimestamp(), o.getTimestamp());
        }

        public abstract Response execute();

        public abstract int sendToPeer(IBankServicePeer peer) throws IOException, InterruptedException;

    }

    static class CreateAccountRequest extends Request {
        CreateAccountRequest(Source request_source, int timestamp, int server_id) {
            super(request_source, timestamp, server_id);
        }

        public String generateLogSuffix() {
            return getRequestName();
        }

        public CreateAccountResponse execute() {
            Bank bank = Bank.getInstance();
            int uuid = bank.createAccount();
            _logger.log(getExecuteLogMessage());
            return new CreateAccountResponse(uuid);
        }

        public int sendToPeer(IBankServicePeer peer) throws IOException, InterruptedException {
            return peer.createAccount(getTimestamp(), getServerId());
        }
    }

    static class DepositRequest extends Request {
        private final int _uuid;
        private final int _amount;

        DepositRequest(Source request_source, int timestamp, int server_id, int uuid, int amount) {
            super(request_source, timestamp, server_id);
            _uuid = uuid;
            _amount = amount;
        }

        public String generateLogSuffix() {
            return String.format("%s %d %d", getRequestName(), _uuid, _amount);
        }

        public int getUuid() {
            return _uuid;
        }

        public int getAmount() {
            return _amount;
        }

        public DepositResponse execute() {
            Bank bank = Bank.getInstance();
            Status status = bank.deposit(getUuid(), getAmount());
            _logger.log(getExecuteLogMessage());
            return new DepositResponse(status);
        }

        public int sendToPeer(IBankServicePeer peer) throws IOException, InterruptedException {
            return peer.deposit(getTimestamp(), getServerId(), getUuid(), getAmount());
        }
    }

    static class GetBalanceRequest extends Request {
        private final int _uuid;

        GetBalanceRequest(Source request_source, int timestamp, int server_id, int uuid) {
            super(request_source, timestamp, server_id);
            _uuid = uuid;
        }

        public String generateLogSuffix() {
            return String.format("%s %d", getRequestName(), _uuid);
        }

        public int getUuid() {
            return _uuid;
        }

        public GetBalanceResponse execute() {
            Bank bank = Bank.getInstance();
            int balance = bank.getBalance(getUuid());
            _logger.log(getExecuteLogMessage());
            return new GetBalanceResponse(balance);
        }

        public int sendToPeer(IBankServicePeer peer) throws IOException, InterruptedException {
            return peer.getBalance(getTimestamp(), getServerId(), getUuid());
        }
    }

    static class TransferRequest extends Request {
        private final int _source_uuid;
        private final int _target_uuid;
        private final int _amount;

        TransferRequest(Source request_source, int timestamp, int server_id, int source_uuid, int target_uuid, int amount) {
            super(request_source, timestamp, server_id);
            _source_uuid = source_uuid;
            _target_uuid = target_uuid;
            _amount = amount;
        }

        public String generateLogSuffix() {
            return String.format("%s %d %d %d", getRequestName(), _source_uuid, _target_uuid, _amount);
        }

        public int getSourceUuid() {
            return _source_uuid;
        }

        public int getTargetUuid() {
            return _target_uuid;
        }

        public int getAmount() {
            return _amount;
        }

        public TransferResponse execute() {
            Bank bank = Bank.getInstance();
            Status status = bank.transfer(getSourceUuid(), getTargetUuid(), getAmount());
            _logger.log(getExecuteLogMessage());
            return new TransferResponse(status);
        }

        public int sendToPeer(IBankServicePeer peer) throws IOException, InterruptedException {
            return peer.transfer(getTimestamp(), getServerId(), getSourceUuid(), getTargetUuid(), getAmount());
        }
    }

    // endregion

    private final PriorityQueue<Request> _queue = new PriorityQueue<Request>();

    private static StateMachine _instance;
    public synchronized static StateMachine getInstance() {
        if (_instance == null)
            _instance = new StateMachine();
        return _instance;
    }

    public synchronized void enqueue(Request request) {
        _logger.log(request.getEnqueueLogMessage());
        _queue.add(request);
    }

    public synchronized Response execute(Request request) throws InterruptedException {
        Request front = _queue.peek();
        if (front != null) {
            if (request.compareTo(front) != 0)
                request.wait();

            front = _queue.poll();
            return front.execute();
        }
        else throw new NullPointerException("Response lost and thus cannot be returned");
    }

    public synchronized Response executeImmediately(int timestamp, int server_id, int local_server_id) throws NullPointerException {
        Request matching_request = null;
        for (Request request : _queue) {
            if (request.getTimestamp() == timestamp && request.getServerId() == server_id) {
                matching_request = request;
                break;
            }
        }

        if (matching_request == null) throw new NullPointerException("No matching request to remove");
        _queue.remove(matching_request);
        Response matching_response = matching_request.execute();

        Request next_request = _queue.peek();
        if (next_request != null && next_request.getServerId() == local_server_id)
            next_request.notify();

        return matching_response;
    }
}
