import javax.swing.text.html.HTMLDocument;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BankServerThread extends Thread {
    protected Socket _socket;
    protected ObjectOutputStream _obj_out;
    protected ObjectInputStream _obj_in;

    protected IBank _bank;

    BankServerThread(Socket socket) throws IOException {
        System.out.println("New client.");
        _socket = socket;
        _obj_out = new ObjectOutputStream(_socket.getOutputStream());
        _obj_in  = new ObjectInputStream(_socket.getInputStream());
    }

    // region Service Routines
    private void handleRequest(Packet.CreateAccountRequest request) throws IOException {
        Packet.CreateAccountResponse response = new Packet.CreateAccountResponse();
        response.setUuid(_bank.createAccount());
        _obj_out.writeObject(response);
    }

    private void handleRequest(Packet.DepositRequest request) throws IOException {
        Packet.DepositResponse response = new Packet.DepositResponse();
        response.setStatus(_bank.deposit(request.getUuid(), request.getAmount()));
        _obj_out.writeObject(response);
    }

    private void handleRequest(Packet.GetBalanceRequest request) throws IOException {
        Packet.GetBalanceResponse response = new Packet.GetBalanceResponse();
        response.setAmount(_bank.getBalance(request.getUuid()));
        _obj_out.writeObject(response);
    }

    private void handleRequest(Packet.TransferRequest request) throws IOException {
        Packet.TransferResponse response = new Packet.TransferResponse();

        _obj_out.writeObject(response);
    }

    // endregion

    public void run() {
        try {
            // create input/output stream abstractions
            Packet.Base request = (Packet.Base) _obj_in.readObject();

            switch (request.getRequestId()) {
                case createAccount:
                    handleRequest((Packet.CreateAccountRequest)request);
                    break;
                case deposit:
                    handleRequest((Packet.DepositRequest)request);
                    break;
                case getBalance:
                    handleRequest((Packet.GetBalanceRequest)request);
                    break;
                case transfer:
                    handleRequest((Packet.TransferRequest)request);
                    break;
            }

            // close client socket connect. can't we just do this in the finally block?
            System.out.println("Client exit.");
        }
        catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                _obj_in.close();
                _obj_out.close();
                _socket.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
