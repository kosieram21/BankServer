enum Status {
    Ok,
    Failed
}

interface IBank {
    int createAccount();
    Status deposit(int uuid, int amount);
    int getBalance(int uuid);
    Status transfer(int source_uuid, int target_uuid, int amount);
}