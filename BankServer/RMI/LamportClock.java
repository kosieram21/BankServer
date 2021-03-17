package BankServer.RMI;

import BankServer.Bank;

import java.io.IOException;

public class LamportClock {
    private int _value = 0;

    public int getValue() {
        return _value;
    }

    public synchronized void advance() {
        _value++;
    }

    public synchronized void merge(LamportClock other) {
        _value = Math.max(getValue(), other.getValue());
    }

    private static LamportClock _instance;
    public synchronized static LamportClock getInstance() {
        if (_instance == null)
            _instance = new LamportClock();
        return _instance;
    }
}
