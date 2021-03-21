package BankServer;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.List;
import java.util.Random;

public class Client {
    static class WorkerThread extends Thread {
        private final List<IBankService> _bank_services;
        private final int _client_id;

        WorkerThread(List<IBankService> bank_services, int client_id) {
            _bank_services = bank_services;
            _client_id = client_id;
        }

        public void run() {
            try {
                LogFile.Client log = LogFile.Client.getInstance();
                final int transfer_amount = 10;
                final Random rng = new Random();
                for(int i = 0; i < 200; i++) {
                    IBankService bank_service = _bank_services.get(rng.nextInt(_bank_services.size()));
                    int source_uuid = rng.nextInt(20);
                    int target_uuid = rng.nextInt(20);
                    log.log(String.format("%d Request transfer(%d, %d, %d)", _client_id, source_uuid, target_uuid, transfer_amount));
                    Status status = bank_service.transfer(source_uuid, target_uuid, transfer_amount);
                    log.log(String.format("%d Response %s", _client_id, status));
                }
            }
            catch (IOException | InterruptedException | NotBoundException ex) {
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
        final ConfigFile.Entry entry = config_file.removeEntry(0);
        final IBankService lowest = service_manager.getService(entry, ServiceManager.BANK_SERVICE);
        final List<IBankService> bank_services = service_manager.getServices(config_file, ServiceManager.BANK_SERVICE);
        bank_services.add(lowest);

        Client.WorkerThread[] threads = new Client.WorkerThread[thread_count];
        for(int i = 0; i < thread_count; i++) {
            threads[i] = new Client.WorkerThread(bank_services, client_id);
            threads[i].run();
        }

        for(int i = 0; i < thread_count; i++) {
            try { threads[i].join(); }
            catch (InterruptedException ex) { ex.printStackTrace(); }
        }

        lowest.halt(client_id);
    }
}
