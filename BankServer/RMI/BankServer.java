package BankServer.RMI;

import BankServer.Bank;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;

public class BankServer {
    private static void initializeDataState() throws IOException {
        Bank bank = Bank.getInstance();
        for(int i = 0; i < 20; i++) {
            int uuid = bank.createAccount();
            bank.deposit(uuid, 1000);
        }
        System.out.println("Data state initialized!");
    }

    private static Registry getRmiRegistry(int port) throws RemoteException {
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(port);
        }
        catch (RemoteException e) {
            LocateRegistry.createRegistry(port);
            registry = LocateRegistry.getRegistry(port);
        }
        return registry;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new RuntimeException("Syntax: tcp.BankServer port-number");
        final int server_id = Integer.parseInt(args[0]);
        final ConfigFile config_file = ConfigFile.parse(args[1]);

        initializeDataState();

        BankService bank_service = new BankService(server_id, config_file);
        IBankService bank_service_stub = (IBankService)UnicastRemoteObject.exportObject(bank_service, 0);

        BankServicePeer bank_service_peer = new BankServicePeer(server_id);
        IBankServicePeer bank_service_peer_stub = (IBankServicePeer)UnicastRemoteObject.exportObject(bank_service_peer, 0);

        final ConfigFile.Entry local_server = config_file.getEntry(server_id);
        final int port = local_server.getRmiRegistryPort();
        Registry local_registry = getRmiRegistry(port);
        local_registry.bind(ServiceNames.BANK_SERVICE, bank_service_stub);
        local_registry.bind(ServiceNames.BANK_SERVICE_PEER, bank_service_peer_stub);
    }
}
