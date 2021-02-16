import java.net.*;
import java.io.*;

public class BankServer extends Thread {
    protected Socket _socket;
    protected IBank _bank;

    BankServer(Socket socket) {
        System.out.println("New client.");
        _socket = socket;
    }

    public void run() {
        try {
            // create input/output stream abstractions
            InputStream istream = _socket.getInputStream();
            OutputStream ostream = _socket.getOutputStream();

            // receive request and send it back as the response
            byte buffer[] = new byte[512];
            int count;
            while ((count = istream.read(buffer)) >= 0) {
                String msg = new String(buffer);
                String outMsg = msg.toUpperCase();
                byte[] outBuf = outMsg.getBytes();
                ostream.write(outBuf, 0, outBuf.length);
                ostream.flush();
                System.out.write(buffer, 0, count);
                System.out.flush();
            }

            // close client socket connect. can't we just do this in the finally block?
            System.out.println("Client exit.");
            _socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                _socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String args[]) throws IOException {
        // create server socket that 'listens' for connections request from clients
        if (args.length != 1) throw new RuntimeException("Syntax: BankServer port-number");
        System.out.println("Starting on port " + args[0]);
        ServerSocket server = new ServerSocket(Integer.parseInt(args[0]));

        // as long as server is alive wait for client request and process them on worker background threads
        while (true) {
            // wait for client TCP socket connection
            System.out.println("Waiting for a client request");
            Socket client = server.accept();
            System.out.println("Received request from " + client.getInetAddress());

            // create background worker thread to handle client traffic
            System.out.println("Starting worker thread...");
            BankServer thread = new BankServer(client);
            thread.start();
        }
    }
}