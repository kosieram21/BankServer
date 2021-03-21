package BankServer.RMI;

import BankServer.Status;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BankClient {
    static class WorkerThread extends Thread {
        private final List<IBankService> _bank_services;

        WorkerThread(List<IBankService> bank_services) { _bank_services = bank_services; }

        public void run() {
            try {
                final int transfer_amount = 10;
                final Random rng = new Random();
                for(int i = 0; i < 200; i++) {
                    IBankService bank_service = _bank_services.get(rng.nextInt(_bank_services.size()));
                    int source_uuid = rng.nextInt(21) + 1;
                    int target_uuid = rng.nextInt(21) + 1;
                    Status status = bank_service.transfer(source_uuid, target_uuid, transfer_amount);
                }
            }
            catch (IOException | InterruptedException | NotBoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static List<IBankService> initializeServerConnections(ConfigFile config_file) throws RemoteException, NotBoundException, MalformedURLException {
        final List<IBankService> servers = new ArrayList<IBankService>();
        for(ConfigFile.Entry entry : config_file) {
            final String bank_service_name = "//" + entry.getHostname() + ":" + entry.getRmiRegistryPort() + "/" + ServiceNames.BANK_SERVICE;
            IBankService server = (IBankService) Naming.lookup(bank_service_name);
            servers.add(server);
        }
        return servers;
    }

    public static void main(String[] args) throws Exception {
        // parse command line arguments
        if (args.length != 4) throw new RuntimeException("hostname and port number as arguments");

        final int client_id = Integer.parseInt(args[0]);
        final int thread_count = Integer.parseInt(args[1]);
        final ConfigFile config_file = ConfigFile.parse(args[2]);

        final List<IBankService> bank_services = initializeServerConnections(config_file);
        BankClient.WorkerThread[] threads = new BankClient.WorkerThread[thread_count];
        for(int i = 0; i < thread_count; i++) {
            threads[i] = new BankClient.WorkerThread(bank_services);
            threads[i].run();
        }

        for(int i = 0; i < thread_count; i++) {
            try { threads[i].join(); }
            catch (InterruptedException ex) { ex.printStackTrace(); }
        }
    }
}
