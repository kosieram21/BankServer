package BankServer.RMI;

import BankServer.Bank;

public class BankServer {
    private static final int NUM_ACCOUNTS = 20;
    private static final int STARTING_BALANCE = 1000;

    private static void initializeDataState() {
        Bank bank = Bank.getInstance();
        for(int i = 0; i < NUM_ACCOUNTS; i++) {
            int uuid = bank.createAccount();
            bank.deposit(uuid, STARTING_BALANCE);
        }
        System.out.println("Data state initialized!");
    }

    private static void logDataState() {
        ServerLog log = ServerLog.getInstance();
        Bank bank = Bank.getInstance();
        int sum = 0;
        for(int i = 1; i < NUM_ACCOUNTS + 1; i++) {
            int balance = bank.getBalance(i);
            sum += balance;
            log.log(String.format("account %d's balance is currently %d", i, balance));
        }
        log.log(String.format("total balance across all accounts is %d", sum));
    }

    public static void shutdown() {
        logDataState();
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new RuntimeException("Syntax: RMI.BankServer server-id config-file num-clients");
        final int server_id = Integer.parseInt(args[0]);
        final ConfigFile config_file = ConfigFile.parse(args[1]);
        config_file.removeEntry(server_id);
        final int num_clients = Integer.parseInt(args[2]);

        ServerLog.getInstance().initialize(server_id);
        initializeDataState();

        final ConfigFile.Entry local_server = config_file.getEntry(server_id);
        final int port = local_server.getRmiRegistryPort();
        BankService bank_service = new BankService(server_id, config_file, num_clients);
        BankServicePeer bank_service_peer = new BankServicePeer(server_id);

        ServiceManager.<IBankService>bindService(bank_service, ServiceManager.BANK_SERVICE, server_id, port);
        ServiceManager.<IBankServicePeer>bindService(bank_service_peer, ServiceManager.BANK_SERVICE_PEER, server_id, port);
    }
}
