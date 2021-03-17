package BankServer.RMI;

import java.util.EventListener;

public interface IRequestProcessedListener extends EventListener {
    void RequestProcessed(RequestProcessedEvent event);
}
