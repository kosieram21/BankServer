package BankServer.RMI;

import BankServer.Status;

import java.util.EventObject;

public class RequestProcessedEvent extends EventObject {
    private final int _uuid;
    private final Status _status;

    public RequestProcessedEvent(Object source, int uuid, Status status) {
        super(source);
        _uuid = uuid;
        _status = status;
    }

    public int getUuid() {
        return _uuid;
    }

    public Status getStatus() {
        return _status;
    }
}
