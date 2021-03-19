package BankServer.RMI;

import java.util.EventListener;

public interface IRequestProcessedListener extends EventListener {
    void createAccountProcessed(RequestQueue.CreateAccountProcessedEvent event) throws InterruptedException ;
    void depositProcessed(RequestQueue.DepositProcessedEvent event) throws InterruptedException ;
    void getBalanceProcessed(RequestQueue.GetBalanceProcessedEvent event) throws InterruptedException ;
    void transferProcessed(RequestQueue.TransferProcessedEvent event) throws InterruptedException ;
}
