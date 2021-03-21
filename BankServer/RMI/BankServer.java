package BankServer.RMI;

import BankServer.Bank;

import java.io.IOException;

public class BankServer {
    private static void initializeDataState() {
        Bank bank = Bank.getInstance();
        for(int i = 0; i < 20; i++) {
            int uuid = bank.createAccount();
            bank.deposit(uuid, 1000);
        }
        System.out.println("Data state initialized!");
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new RuntimeException("Syntax: RMI.BankServer server-id config-file");
        final int server_id = Integer.parseInt(args[0]);
        final ConfigFile config_file = ConfigFile.parse(args[1]);
        config_file.removeEntry(server_id);

        Bank.getInstance().initializeLogger(server_id);
        initializeDataState();

        final ConfigFile.Entry local_server = config_file.getEntry(server_id);
        final int port = local_server.getRmiRegistryPort();
        BankService bank_service = new BankService(server_id, config_file);
        BankServicePeer bank_service_peer = new BankServicePeer(server_id);

        ServiceManager.<IBankService>bindService(bank_service, ServiceManager.BANK_SERVICE, port);
        ServiceManager.<IBankServicePeer>bindService(bank_service_peer, ServiceManager.BANK_SERVICE_PEER, port);
    }
}
