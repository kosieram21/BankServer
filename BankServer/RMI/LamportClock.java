package BankServer.RMI;

public class LamportClock {
    private int _value = 0;

    private static LamportClock _instance;
    public synchronized static LamportClock getInstance() {
        if (_instance == null) _instance = new LamportClock();
        return _instance;
    }

    public int getValue() {
        return _value;
    }

    public synchronized int advance() {
        return _value++;
    }

    public synchronized int merge(int other) {
        _value = Math.max(getValue(), other);
        return advance();
    }
}
