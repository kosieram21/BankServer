import java.io.IOException;
import java.net.Socket;

public class BankServerThread extends Thread {
    private final BankService _bank_service;

    BankServerThread(Socket socket) throws IOException {
        System.out.println("New client.");
        _bank_service = new BankService(socket);
    }

    public void run() {
        try {
            _bank_service.handleRequest();
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
