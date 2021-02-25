package BankServer.TCP;

import java.io.IOException;
import java.net.Socket;
import BankServer.Status;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.FileHandler;

public final class BankStub extends BankCommunication {
    private final Logger _logger;

    BankStub(Socket socket) throws IOException {
        super(socket);

        _logger = Logger.getLogger(BankStub.class.getName());
        FileHandler handler = new FileHandler("clientLogfile%u.txt");
        _logger.addHandler(handler);
        handler.setFormatter(new SimpleFormatter());
    }

    public int createAccount() throws IOException, ClassNotFoundException {
        Packet.CreateAccountRequest request = new Packet.CreateAccountRequest();
        _logger.info("request: createAccount()");
        _obj_out.writeObject(request);

        Packet.CreateAccountResponse response = (Packet.CreateAccountResponse)_obj_in.readObject();
        _logger.info("response: createAccount() -> " + response.getUuid());
        return response.getUuid();
    }

    public Status deposit(int uuid, int amount) throws IOException, ClassNotFoundException {
        Packet.DepositRequest request = new Packet.DepositRequest();
        request.setUuid(uuid);
        request.setAmount(amount);
        _logger.info("request: deposit(" + uuid + ", " + amount + ")");
        _obj_out.writeObject(request);

        Packet.DepositResponse response = (Packet.DepositResponse)_obj_in.readObject();
        _logger.info("response: deposit(" + uuid + ", " + amount + ") -> " + response.getStatus());
        return response.getStatus();
    }

    public int getBalance(int uuid) throws IOException, ClassNotFoundException {
        Packet.GetBalanceRequest request = new Packet.GetBalanceRequest();
        request.setUuid(uuid);
        _logger.info("request: getBalance(" + uuid + ")");
        _obj_out.writeObject(request);

        Packet.GetBalanceResponse response = (Packet.GetBalanceResponse)_obj_in.readObject();
        _logger.info("response: getBalance(" + uuid + ") -> " + response.getAmount());
        return response.getAmount();
    }

    public Status transfer(int source_uuid, int target_uuid, int amount) throws IOException, ClassNotFoundException {
        Packet.TransferRequest request = new Packet.TransferRequest();
        request.setSourceUuid(source_uuid);
        request.setTargetUuid(target_uuid);
        request.setAmount(amount);
        _logger.info("request: transfer(" + source_uuid + ", " + target_uuid + ", " + amount + ")");
        _obj_out.writeObject(request);

        Packet.TransferResponse response = (Packet.TransferResponse)_obj_in.readObject();
        _logger.info("response: transfer(" + source_uuid + ", " + target_uuid + ", " + amount + ") ->" + response.getStatus());
        return response.getStatus();
    }

    public void exit() throws IOException {
        Packet.ExitRequest request = new Packet.ExitRequest();
        _obj_out.writeObject(request);
        close();
    }
}
