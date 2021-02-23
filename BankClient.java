import java.net.*;
import java.io.*;

public class BankClient {
    public static void main(String args[]) throws IOException, ClassNotFoundException {
        // parse command line arguments
        if (args.length != 2) throw new RuntimeException("hostname and port number as arguments");
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        // create TCP socket connection with server
        System.out.println("Connecting to " + host + ":" + port + "..");
        Socket socket = new Socket(host, port);
        System.out.println("Connected.");

        BankStub bank_stub = new BankStub(socket);

        System.out.println("creating account");
        int uuid = bank_stub.createAccount();
        System.out.println(uuid);

        System.out.println("depositing");
        bank_stub.deposit(uuid, 100);
        System.out.println(bank_stub.getBalance(uuid));

        System.out.println("depositing");
        bank_stub.deposit(uuid, 100);
        System.out.println(bank_stub.getBalance(uuid));

        bank_stub.exit();
    }
}