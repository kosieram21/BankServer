package BankServer.RMI;

public class LamportClock {
    private int _value = 0;

    public int getValue() {
        return _value;
    }

    public void advance() {
        _value++;
    }

    public void merge(LamportClock other) {
        _value = Math.max(getValue(), other.getValue());
    }
}
