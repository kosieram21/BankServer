import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class BankService {
    private final Socket _socket;
    private final ObjectOutputStream _obj_out;
    private final ObjectInputStream _obj_in;

    private IBank _bank;

    BankService(Socket socket) throws IOException {
        _socket = socket;
        _obj_out = new ObjectOutputStream(_socket.getOutputStream());
        _obj_in  = new ObjectInputStream(_socket.getInputStream());
    }

    public void close() throws IOException {
        _obj_in.close();
        _obj_out.close();
        _socket.close();
    }

    public void handleRequest() throws IOException, ClassNotFoundException {
        // create input/output stream abstractions
        Packet.Request request = (Packet.Request) _obj_in.readObject();

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
    }

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
        response.setStatus(_bank.transfer(request.getSourceUuid(), request.getTargetUuid(), request.getAmount()));
        _obj_out.writeObject(response);
    }
}
