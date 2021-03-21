package BankServer.RMI;

import BankServer.Status;

import java.io.IOException;
import java.rmi.NotBoundException;
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

    public static void main(String[] args) throws Exception {
        // parse command line arguments
        if (args.length != 3) throw new RuntimeException("Syntax: RMI.BankServer client-id thread-count config-file");
        final int client_id = Integer.parseInt(args[0]);
        final int thread_count = Integer.parseInt(args[1]);
        final ConfigFile config_file = ConfigFile.parse(args[2]);

        final ConfigFile.Entry entry = config_file.removeEntry(0);
        final IBankService lowest = ServiceManager.getService(entry, ServiceManager.BANK_SERVICE);

        final List<IBankService> bank_services = ServiceManager.getServices(config_file, ServiceManager.BANK_SERVICE);
        bank_services.add(lowest);
        lowest.declarePresence(client_id);

        BankClient.WorkerThread[] threads = new BankClient.WorkerThread[thread_count];
        for(int i = 0; i < thread_count; i++) {
            threads[i] = new BankClient.WorkerThread(bank_services);
            threads[i].run();
        }

        for(int i = 0; i < thread_count; i++) {
            try { threads[i].join(); }
            catch (InterruptedException ex) { ex.printStackTrace(); }
        }

        lowest.halt(client_id);
    }
}
