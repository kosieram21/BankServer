package BankServer.RMI;

import BankServer.Status;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.Random;

public class BankClient {
    static class WorkerThread extends Thread {
        private final IBankService _bank_service;
        private final int[] _uuids;
        private final int _iterations;

        WorkerThread(IBankService bank_service, int[] uuids, int iterations) throws IOException {
            _bank_service = bank_service;
            _uuids = uuids;
            _iterations = iterations;
        }

        public void run() {
            try {
                final int transfer_amount = 10;
                final Random rng = new Random();
                for(int i = 0; i < _iterations; i++) {
                    int source_uuid = _uuids[rng.nextInt(_uuids.length)];
                    int target_uuid = _uuids[rng.nextInt(_uuids.length)];
                    Status status = _bank_service.transfer(source_uuid, target_uuid, transfer_amount);
                }
            }
            catch (IOException | InterruptedException | NotBoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        // parse command line arguments
        if (args.length != 4) throw new RuntimeException("hostname and port number as arguments");

        final String host = args[0];
        final int port = Integer.parseInt(args[1]);
        final int thread_count = Integer.parseInt(args[2]);
        final int iteration_count = Integer.parseInt(args[3]);

        final String bank_service_name = "//" + host + ":" + port + "/" + ServiceNames.BANK_SERVICE_RMI;
        IBankService bank_service = (IBankService) Naming.lookup(bank_service_name);

        final int num_accounts = 100;
        final int base_amount = 100;

        int[] uuids = new int[num_accounts];
        for(int i = 0; i < num_accounts; i++) {
            uuids[i] = bank_service.createAccount();
            bank_service.deposit(uuids[i], base_amount);
        }

        int sum = 0;
        for(int i = 0; i < num_accounts; i++)
            sum = sum + bank_service.getBalance(uuids[i]);
        System.out.println(sum);

        BankClient.WorkerThread[] threads = new BankClient.WorkerThread[thread_count];
        for(int i = 0; i < thread_count; i++) {
            threads[i] = new BankClient.WorkerThread(bank_service, uuids, iteration_count);
            threads[i].run();
        }

        for(int i = 0; i < thread_count; i++) {
            try { threads[i].join(); }
            catch (InterruptedException ex) { ex.printStackTrace(); }
        }

        sum = 0;
        for(int i = 0; i < num_accounts; i++)
            sum = sum + bank_service.getBalance(uuids[i]);
        System.out.println(sum);
    }
}
