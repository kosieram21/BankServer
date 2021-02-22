enum Status {
    Ok,
    Failed;

    public static Status convert(byte val) {
        return Status.values()[val];
    }
}

interface IBank {
    int createAccount();
    Status deposit(int uuid, int amount);
    int getBalance(int uuid);
    Status transfer(int source_uuid, int target_uuid, int amount);
}