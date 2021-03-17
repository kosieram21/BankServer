package BankServer.RMI;

import BankServer.Bank;
import BankServer.Status;

import java.io.IOException;
import java.rmi.RemoteException;

public class BankServiceRMI implements IBankServiceRMI {
    private final Bank _bank;
    private  final LamportClock _clock;

    public BankServiceRMI () throws RemoteException, IOException {
        super();
        _bank = Bank.getInstance();
        _clock = LamportClock.getInstance();
    }

    @Override
    public int createAccount() throws RemoteException {
        return _bank.createAccount();
    }

    @Override
    public Status deposit(int uuid, int amount) throws RemoteException {
        return _bank.deposit(uuid, amount);
    }

    @Override
    public int getBalance(int uuid) throws RemoteException {
        return _bank.getBalance(uuid);
    }

    @Override
    public Status transfer(int source_uuid, int target_uuid, int amount) throws RemoteException {
        return _bank.transfer(source_uuid, target_uuid, amount);
    }
}
