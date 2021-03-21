package BankServer.RMI;

import BankServer.Status;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BankService implements IBankService {
    private  final LamportClock _clock;
    private final RequestQueue _request_queue;

    private final int _local_server_id;
    private final ConfigFile _config_file;

    private List<IBankServicePeer> _peers;

    public BankService(int local_server_id, ConfigFile config_file) {
        super();
        _clock = LamportClock.getInstance();
        _request_queue = RequestQueue.getInstance();

        _local_server_id = local_server_id;
        _config_file = config_file;
    }

    @Override
    public int createAccount() throws IOException, InterruptedException, NotBoundException {
        int timestamp = _clock.advance();
        RequestQueue.CreateAccountRequest request = new RequestQueue.CreateAccountRequest(timestamp, _local_server_id);
        RequestQueue.CreateAccountResponse response = (RequestQueue.CreateAccountResponse)executeRequest(request);
        return response.getUuid();
    }

    @Override
    public Status deposit(int uuid, int amount) throws IOException, InterruptedException, NotBoundException {
        int timestamp = _clock.advance();
        RequestQueue.DepositRequest request = new RequestQueue.DepositRequest(timestamp, _local_server_id, uuid, amount);
        RequestQueue.DepositResponse response = (RequestQueue.DepositResponse)executeRequest(request);
        return response.getStatus();
    }

    @Override
    public int getBalance(int uuid) throws IOException, InterruptedException, NotBoundException {
        int timestamp = _clock.advance();
        RequestQueue.GetBalanceRequest request = new RequestQueue.GetBalanceRequest(timestamp, _local_server_id, uuid);
        RequestQueue.GetBalanceResponse response = (RequestQueue.GetBalanceResponse)executeRequest(request);
        return response.getBalance();
    }

    @Override
    public Status transfer(int source_uuid, int target_uuid, int amount)
            throws IOException, InterruptedException, NotBoundException
    {
        int timestamp = _clock.advance();
        RequestQueue.TransferRequest request = new RequestQueue.TransferRequest(timestamp, _local_server_id, source_uuid, target_uuid, amount);
        RequestQueue.TransferResponse response = (RequestQueue.TransferResponse)executeRequest(request);
        return response.getStatus();
    }

    private RequestQueue.Response executeRequest(RequestQueue.Request request)
            throws IOException, NotBoundException, InterruptedException
    {
        _request_queue.enqueue(request);
        multicast(request);
        RequestQueue.Response response = _request_queue.execute(request);
        multicast(peer -> peer.execute(request.getTimestamp(), request.getServerId()));
        return response;
    }

    private void multicast(RequestQueue.Request request) throws InterruptedException, IOException, NotBoundException {
        AtomicInteger max_timestamp = new AtomicInteger(0);
        multicast(peer -> {
            int timestamp = request.sendToPeer(peer);
            max_timestamp.set(Math.max(max_timestamp.get(), timestamp));
        });
        _clock.merge(max_timestamp.get());
    }

    private void multicast(ThrowingConsumer<IBankServicePeer> send_message)
            throws RemoteException, NotBoundException, MalformedURLException {
        if(_peers == null) _peers = ServiceManager.getServices(_config_file, ServiceManager.BANK_SERVICE_PEER);
        for (IBankServicePeer peer : _peers)
            send_message.accept(peer);
    }
}
