import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

public class BankServer {
    static class WorkerThread extends Thread {
        private final BankService _bank_service;

        WorkerThread(Socket socket) throws IOException {
            System.out.println("New client.");
            _bank_service = new BankService(socket);
        }

        public void run() {
            try {
                while(_bank_service.handleRequest()) { }
            }
            catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            finally {
                try {
                    _bank_service.close();
                    System.out.println("Client exit.");
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String args[]) throws IOException {
        // create server socket that 'listens' for connections request from clients
        if (args.length != 1) throw new RuntimeException("Syntax: BankServer port-number");
        int port = Integer.parseInt(args[0]);

        System.out.println("Starting on port " + port);
        ServerSocket server = new ServerSocket(port);

        // as long as server is alive wait for client request and process them on worker background threads
        while (true) {
            // wait for client TCP socket connection
            System.out.println("Waiting for a client request");
            Socket client = server.accept();
            System.out.println("Received request from " + client.getInetAddress());

            // create background worker thread to handle client traffic
            System.out.println("Starting worker thread...");
            WorkerThread thread = new WorkerThread(client);
            thread.start();
        }
    }
}