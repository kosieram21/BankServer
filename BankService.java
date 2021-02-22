import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class BankService {
    private final Socket _socket;
    private final ObjectOutputStream _obj_out;
    private final ObjectInputStream _obj_in;

    private final Bank _bank;

    BankService(Socket socket) throws IOException {
        _socket = socket;
        _obj_out = new ObjectOutputStream(_socket.getOutputStream());
        _obj_in  = new ObjectInputStream(_socket.getInputStream());
        _bank = Bank.getInstance();
    }

    public void close() throws IOException {
        _obj_in.close();
        _obj_out.close();
        _socket.close();
    }

    public boolean handleRequest() throws IOException, ClassNotFoundException {
        boolean exit_session = false;

        Packet.Request request = (Packet.Request) _obj_in.readObject();
        switch (request.getRequestId()) {
            case createAccount -> handleRequest((Packet.CreateAccountRequest) request);
            case deposit -> handleRequest((Packet.DepositRequest) request);
            case getBalance -> handleRequest((Packet.GetBalanceRequest) request);
            case transfer -> handleRequest((Packet.TransferRequest) request);
            case exit -> exit_session = true;
        }

        return !exit_session;
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
