package BankServer;

public enum Status {
    Ok,
    Failed;

    public static Status convert(byte val) { return Status.values()[val]; }
}
