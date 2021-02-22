import java.io.IOException;
import java.net.Socket;

public final class BankStub extends BankCommunication {
    BankStub(Socket socket) throws IOException { super(socket); }

    public int createAccount() throws IOException, ClassNotFoundException {
        Packet.CreateAccountRequest request = new Packet.CreateAccountRequest();
        _obj_out.writeObject(request);

        Packet.CreateAccountResponse response = (Packet.CreateAccountResponse)_obj_in.readObject();
        return response.getUuid();
    }

    public Status deposit(int uuid, int amount) throws IOException, ClassNotFoundException {
        Packet.DepositRequest request = new Packet.DepositRequest();
        request.setUuid(uuid);
        request.setAmount(amount);
        _obj_out.writeObject(request);

        Packet.DepositResponse response = (Packet.DepositResponse)_obj_in.readObject();
        return response.getStatus();
    }

    public int getBalance(int uuid) throws IOException, ClassNotFoundException {
        Packet.GetBalanceRequest request = new Packet.GetBalanceRequest();
        request.setUuid(uuid);
        _obj_out.writeObject(request);

        Packet.GetBalanceResponse response = (Packet.GetBalanceResponse)_obj_in.readObject();
        return response.getAmount();
    }

    public Status transfer(int source_uuid, int target_uuid, int amount) throws IOException, ClassNotFoundException {
        Packet.TransferRequest request = new Packet.TransferRequest();
        request.setSourceUuid(source_uuid);
        request.setTargetUuid(target_uuid);
        request.setAmount(amount);
        _obj_out.writeObject(request);

        Packet.TransferResponse response = (Packet.TransferResponse)_obj_in.readObject();
        return response.getStatus();
    }

    public void exit() throws IOException {
        Packet.ExitRequest request = new Packet.ExitRequest();
        _obj_out.writeObject(request);
        close();
    }
}
