import java.net.*;
import java.io.*;

public class EchoClient {
    public static void main(String args[]) throws IOException {
        // parse command line arguments
        if (args.length != 2) throw new RuntimeException("hostname and port number as arguments");
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        // create TCP socket connection with server
        System.out.println("Connecting to " + host + ":" + port + "..");
        Socket socket = new Socket(host, port);
        System.out.println("Connected.");

        // create input/output stream abstractions
        OutputStream rawOut = socket.getOutputStream();
        InputStream rawIn = socket.getInputStream();
        BufferedReader buffreader = new BufferedReader(new InputStreamReader(rawIn));
        PrintWriter serverWriter = new PrintWriter(new OutputStreamWriter(rawOut));
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

        // send request
        String line;
        while ((line = keyboard.readLine()) != null) {
            serverWriter.println(line);
            serverWriter.flush();
        }

        socket.shutdownOutput();

        // receive response
        while (buffreader.ready()) {
            if ((line = buffreader.readLine()) != null)
                System.out.println(line);
        }
    }
}