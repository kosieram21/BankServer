package BankServer;

import java.util.Hashtable;

public class Bank {
    private final Hashtable<Integer, Account> _accounts = new Hashtable<Integer, Account>();

    static class Account {
        private int _uuid;
        private int _balance;

        public synchronized int getUuid() {
            return _uuid;
        }
        public synchronized void setUuid(int uuid) {
            _uuid = uuid;
        }

        public synchronized int getBalance() {
            return _balance;
        }
        public synchronized void setBalance(int balance) {
            _balance = balance;
        }
    }

    private Bank() {}

    private static Bank _instance;
    public synchronized static Bank getInstance() {
        if (_instance == null)
            _instance = new Bank();
        return _instance;
    }

    public int createAccount() {
        Account account = new Account();
        account.setUuid(getNextUuid());
        account.setBalance(0);
        _accounts.put(account.getUuid(), account);
        return account.getUuid();
    }

    public Status deposit(int uuid, int amount) {
        if(!_accounts.containsKey(uuid))
            return Status.Failed;

        Account account = _accounts.get(uuid);
        synchronized (account) {
            int balance = account.getBalance();
            account.setBalance(balance + amount);
        }

        return Status.Ok;
    }

    public int getBalance(int uuid) {
        Account account = _accounts.get(uuid);
        return account.getBalance();
    }

    public Status transfer(int source_uuid, int target_uuid, int amount) {
        if (!_accounts.containsKey(source_uuid) || !_accounts.containsKey(target_uuid))
            return Status.Failed;

        if(source_uuid == target_uuid)
            return Status.Failed;

        Account source_account = _accounts.get(source_uuid);
        Account target_account = _accounts.get(target_uuid);

        if (source_account.getBalance() < amount)
            return Status.Failed;

        // if one thread synchronizes source_account then target_account while another synchronizes
        // target_account then source_account there is a possibility of a dead lock. if we always synchronized
        // in the same order (smaller uuid first) the deadlock is eliminated.
        Account first = source_account.getUuid() < target_account.getUuid() ? source_account : target_account;
        Account second = source_account.getUuid() >= target_account.getUuid() ? source_account : target_account;

        //synchronized (source_account) {
        //    synchronized (target_account) {
        synchronized (first) {
            synchronized (second) {
                int source_balance = source_account.getBalance();
                int target_balance = target_account.getBalance();
                source_account.setBalance(source_balance - amount);
                target_account.setBalance(target_balance + amount);
            }
        }

        return Status.Ok;
    }

    private int _nextUuid = 0;
    private synchronized int getNextUuid() {
        return _nextUuid++;
    }
}