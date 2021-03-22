package BankServer;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class Client {
    static class WorkerThread extends Thread {
        private final int _client_id;
        private final HashMap<Integer, IBankService> _bank_services;

        WorkerThread(int client_id, HashMap<Integer, IBankService> bank_services) {
            _client_id = client_id;
            _bank_services = bank_services;
        }

        private Integer getServerId(int index) throws Exception {
            Set<Integer> keys = _bank_services.keySet();
            int i = 0;
            for(Integer key : keys) {
                if(i++ == index)
                    return key;
            }
            throw new Exception(String.format("%d is out of range", index));
        }

        public void run() {
            try {
                LogFile.Client log = LogFile.Client.getInstance();
                final int transfer_amount = 10;
                final int num_iterations = 200;
                final Random rng = new Random();

                double sum = 0;
                for(int i = 0; i < num_iterations; i++) {
                    int server_id = getServerId(rng.nextInt(_bank_services.size()));
                    IBankService bank_service = _bank_services.get(server_id);
                    int source_uuid = rng.nextInt(20);
                    int target_uuid = rng.nextInt(20);

                    log.log(String.format("%d %d Request transfer(%d, %d, %d)", _client_id, server_id, source_uuid, target_uuid, transfer_amount));
                    long t0 = System.nanoTime();
                    Status status = bank_service.transfer(source_uuid, target_uuid, transfer_amount);
                    long t1 = System.nanoTime();
                    long span = t1 - t0;
                    log.log(String.format("%d Response %s", _client_id, status));
                    sum += span;
                }

                sum = sum / num_iterations;
                System.out.println(String.format("average transfer latency: %.2f ns", sum));
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        // parse command line arguments
        if (args.length != 3) throw new RuntimeException("Syntax: Client client-id thread-count config-file");
        final int client_id = Integer.parseInt(args[0]);
        final int thread_count = Integer.parseInt(args[1]);
        final ConfigFile config_file = ConfigFile.parse(args[2]);

        LogFile.Client.getInstance().initialize(client_id);

        ServiceManager service_manager = ServiceManager.getInstance();
        final HashMap<Integer, IBankService> bank_services = service_manager.getServices(config_file, ServiceManager.BANK_SERVICE);
        final IBankService lowest = bank_services.get(0);

        Client.WorkerThread[] threads = new Client.WorkerThread[thread_count];
        for(int i = 0; i < thread_count; i++) {
            threads[i] = new Client.WorkerThread(client_id, bank_services);
            threads[i].run();
        }

        for(int i = 0; i < thread_count; i++) {
            try { threads[i].join(); }
            catch (InterruptedException ex) { ex.printStackTrace(); }
        }

        lowest.halt(client_id);
    }
}
