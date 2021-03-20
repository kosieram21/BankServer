package BankServer.RMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;

public class BankServer {
    private static List<BankServerReplica> readConfigFile(String filepath) throws FileNotFoundException {
        File file = new File(filepath);
        Scanner scanner = new Scanner(file);

        List<BankServerReplica> replicas = new ArrayList<BankServerReplica>();
        while(scanner.hasNext()) {
            String line = scanner.nextLine();
            String[] parts = line.split("-");

            String host_name = parts[0];
            int pId = Integer.parseInt(parts[1]);
            int rmi_registry_port = Integer.parseInt(parts[2]);

            BankServerReplica replica = new BankServerReplica(host_name, pId, rmi_registry_port);
            replicas.add(replica);
        }

        scanner.close();
        return replicas;
    }

    private static BankServerReplica getReplica(int pId, List<BankServerReplica> replicas) throws Exception {
        for(BankServerReplica replica : replicas) {
            if(replica.getServerId() == pId)
                return replica;
        }

        throw new Exception("Server replica with id: " + String.valueOf(pId) + " not found in config.");
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
        final int pId = Integer.parseInt(args[0]);
        final List<BankServerReplica> peer_servers = readConfigFile(args[1]);

        final BankServerReplica local_server = getReplica(pId, peer_servers);
        peer_servers.remove(local_server);

        BankService bank_service = new BankService(local_server, peer_servers);
        IBankService bank_service_stub = (IBankService)UnicastRemoteObject.exportObject(bank_service, 0);

        BankServicePeer bank_service_peer = new BankServicePeer(local_server);
        IBankServicePeer bank_service_peer_stub = (IBankServicePeer)UnicastRemoteObject.exportObject(bank_service_peer, 0);

        final int port = local_server.getRmiRegistryPort();
        Registry local_registry = getRmiRegistry(port);
        local_registry.bind(ServiceNames.BANK_SERVICE_RMI, bank_service_stub);
        local_registry.bind(ServiceNames.BANK_SERVICE_PEER, bank_service_peer_stub);
    }
}
