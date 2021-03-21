package BankServer;

public class Server {
    private static void initializeDataState() {
        Bank bank = Bank.getInstance();
        for(int i = 0; i < 20; i++) {
            int uuid = bank.createAccount();
            bank.deposit(uuid, 1000);
        }
        System.out.println("Data state initialized!");
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new RuntimeException("Syntax: Server server-id num-clients config-file");
        final int server_id = Integer.parseInt(args[0]);
        final int num_clients = Integer.parseInt(args[1]);
        final ConfigFile config_file = ConfigFile.parse(args[2]);
        final ConfigFile.Entry local_server = config_file.removeEntry(server_id);

        LogFile.Server.getInstance().initialize(server_id);
        initializeDataState();

        BankService bank_service = new BankService(server_id, config_file, num_clients);
        BankServicePeer bank_service_peer = new BankServicePeer(server_id);

        ServiceManager service_manager = ServiceManager.getInstance();
        service_manager.setPort(local_server.getRmiRegistryPort());
        service_manager.setId(server_id);
        service_manager.<IBankService>bindService(ServiceManager.BANK_SERVICE, bank_service);
        service_manager.<IBankServicePeer>bindService(ServiceManager.BANK_SERVICE_PEER, bank_service_peer);
    }
}
