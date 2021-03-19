package BankServer.RMI;

import BankServer.Bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.registry.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;

public class BankServerRMI {
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

        throw new Exception("Server replica of with id: " + String.valueOf(pId) + " not found in config.");
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
        final List<BankServerReplica> replicas = readConfigFile(args[1]);

        final BankServerReplica replica = getReplica(pId, replicas);
        replicas.remove(replica);
        final int port = replica.getRmiRegistryPort();

        BankServiceRMI bank_service = new BankServiceRMI(replica, replicas);
        IBankServiceRMI bank_service_stub = (IBankServiceRMI)UnicastRemoteObject.exportObject(bank_service, 0);
        IBankServicePeer bank_service_peer_stub = null;

        Registry localRegistry = getRmiRegistry(port);
        localRegistry.bind(ServiceNames.BANK_SERVICE_RMI, bank_service_stub);
        localRegistry.bind(ServiceNames.BANK_SERVICE_PEER, bank_service_peer_stub);
    }
}
