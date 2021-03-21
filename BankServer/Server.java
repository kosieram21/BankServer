package BankServer;

public class Server {
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

    private static void printDataState() {
        ServerLog log = ServerLog.getInstance();
        Bank bank = Bank.getInstance();
        int sum = 0;
        for(int i = 0; i < NUM_ACCOUNTS; i++) {
            int balance = bank.getBalance(i);
            sum += balance;
            log.log(String.format("account %d's balance is currently %d", i, balance));
        }
        log.log(String.format("total balance across all accounts is %d", sum));
    }

    public static void shutdown() {
        printDataState();
        // shut down rmi registry?
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new RuntimeException("Syntax: RMI.BankServer server-id num-clients config-file");
        final int server_id = Integer.parseInt(args[0]);
        final int num_clients = Integer.parseInt(args[1]);
        final ConfigFile config_file = ConfigFile.parse(args[2]);
        final ConfigFile.Entry local_server = config_file.removeEntry(server_id);

        ServerLog.getInstance().initialize(server_id);
        initializeDataState();

        final int port = local_server.getRmiRegistryPort();
        BankService bank_service = new BankService(server_id, config_file, num_clients);
        BankServicePeer bank_service_peer = new BankServicePeer(server_id);

        ServiceManager.<IBankService>bindService(bank_service, ServiceManager.BANK_SERVICE, server_id, port);
        ServiceManager.<IBankServicePeer>bindService(bank_service_peer, ServiceManager.BANK_SERVICE_PEER, server_id, port);
    }
}
