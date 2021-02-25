package BankServer.TCP;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import BankServer.Status;

public class BankClient {
    static class WorkerThread extends Thread {
        private final BankStub _bank_stub;
        private final int[] _uuids;
        private final int _iterations;

        WorkerThread(Socket socket, int[] uuids, int iterations) throws IOException {
            _bank_stub = new BankStub(socket);
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
                    Status status = _bank_stub.transfer(source_uuid, target_uuid, transfer_amount);
                }
            }
            catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            finally {
                try {
                    _bank_stub.exit();
                    System.out.println("Client exit.");
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // parse command line arguments
        if (args.length != 4) throw new RuntimeException("hostname and port number as arguments");

        final String host = args[0];
        final int port = Integer.parseInt(args[1]);
        final int thread_count = Integer.parseInt(args[2]);
        final int iteration_count = Integer.parseInt(args[3]);

        // create TCP socket connection with server
        System.out.println("Connecting to " + host + ":" + port + "..");
        Socket socket = new Socket(host, port);
        System.out.println("Connected.");

        BankStub bank_stub = new BankStub(socket);

        final int num_accounts = 100;
        final int base_amount = 100;

        int[] uuids = new int[num_accounts];
        for(int i = 0; i < num_accounts; i++) {
            uuids[i] = bank_stub.createAccount();
            bank_stub.deposit(uuids[i], base_amount);
        }

        int sum = 0;
        for(int i = 0; i < num_accounts; i++)
            sum = sum + bank_stub.getBalance(uuids[i]);
        System.out.println(sum);

        WorkerThread[] threads = new WorkerThread[thread_count];
        for(int i = 0; i < thread_count; i++) {
            threads[i] = new WorkerThread(new Socket(host, port), uuids, iteration_count);
            threads[i].run();
        }

        for(int i = 0; i < thread_count; i++) {
            try { threads[i].join(); }
            catch (InterruptedException ex) { ex.printStackTrace(); }
        }

        sum = 0;
        for(int i = 0; i < num_accounts; i++)
            sum = sum + bank_stub.getBalance(uuids[i]);
        System.out.println(sum);

        bank_stub.exit();
    }
}