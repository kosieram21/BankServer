import java.net.*;
import java.io.*;

public class BankServer {
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
            BankServerThread thread = new BankServerThread(client);
            thread.start();
        }
    }
}