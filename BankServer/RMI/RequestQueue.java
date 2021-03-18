package BankServer.RMI;

import javax.swing.event.EventListenerList;
import java.util.PriorityQueue;
import java.util.Queue;

public class RequestQueue {
    static class Request implements Comparable<Request> {
        private final int _timestamp;
        private final int _processId;

        Request(int timestamp, int processId) {
            _timestamp = timestamp;
            _processId = processId;
        }

        public int getTimestamp() {
            return _timestamp;
        }

        public int getProcessId() {
            return _processId;
        }

        @Override
        public int compareTo(Request o) {
            return Integer.compare(_timestamp, o._timestamp) == 0 ?
                    Integer.compare(_processId, o._processId) :
                    Integer.compare(_timestamp, o._timestamp);
        }
    }

    static class CreateAccountRequest extends Request {
        CreateAccountRequest(int timestamp, int processId) {
            super(timestamp, processId);
        }
    }

    static class DepositRequest extends Request {
        private final int _uuid;
        private final int _amount;

        DepositRequest(int timestamp, int processId, int uuid, int amount) {
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
    }

    static class GetBalanceRequest extends Request {
        private final int _uuid;

        GetBalanceRequest(int timestamp, int processId, int uuid) {
            super(timestamp, processId);
            _uuid = uuid;
        }

        public int getUuid() {
            return _uuid;
        }
    }

    static class TransferRequest extends Request {
        private final int _sourceUuid;
        private final int _targetUuid;
        private final int _amount;

        TransferRequest(int timestamp, int processId, int sourceUuid, int targetUuid, int amount) {
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
    }

    private final PriorityQueue<Request> _queue;
    private final EventListenerList _eventListeners;

    RequestQueue() {
        _queue = new PriorityQueue<Request>();
        _eventListeners = new EventListenerList();
    }

    public void processRequests() {
        // This method should be running in a background thread to process
        // request in the queue according to lamport's method This method should also
        // fire the event processed event to alert the server's client interface of the
        // proper response to send back to the client. An optimization that could be made
        // is to only have the background thread run when there are events in the queue.
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

    private void onRequestProcessed(RequestProcessedEvent event) {
        IRequestProcessedListener[] listeners = _eventListeners.getListeners(IRequestProcessedListener.class);
        for(IRequestProcessedListener listener : listeners)
            listener.RequestProcessed(event);
    }

    private static RequestQueue _instance;
    public synchronized static RequestQueue getInstance() {
        if (_instance == null)
            _instance = new RequestQueue();
        return _instance;
    }
}
