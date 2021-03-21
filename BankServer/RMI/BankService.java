package BankServer.RMI;

import BankServer.Status;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BankService implements IBankService {
    private static final StateMachine.Request.Source REQUEST_SOURCE = StateMachine.Request.Source.Client;

    private  final LamportClock _clock;
    private final StateMachine _state_machine;

    private final int _local_server_id;
    private final ConfigFile _config_file;

    private List<IBankServicePeer> _peers;

    private final int _num_clients;
    private final List<Integer> _halted_clients;

    public BankService(int local_server_id, ConfigFile config_file, int num_clients) {
        super();
        _clock = LamportClock.getInstance();
        _state_machine = StateMachine.getInstance();

        _local_server_id = local_server_id;
        _config_file = config_file;

        _num_clients = num_clients;
        _halted_clients = new ArrayList<Integer>(_num_clients);
    }

    @Override
    public int createAccount() throws IOException, InterruptedException, NotBoundException {
        int timestamp = _clock.advance();
        StateMachine.CreateAccountRequest request = new StateMachine.CreateAccountRequest(REQUEST_SOURCE, timestamp, _local_server_id);
        StateMachine.CreateAccountResponse response = (StateMachine.CreateAccountResponse)executeRequest(request);
        return response.getUuid();
    }

    @Override
    public Status deposit(int uuid, int amount) throws IOException, InterruptedException, NotBoundException {
        int timestamp = _clock.advance();
        StateMachine.DepositRequest request = new StateMachine.DepositRequest(REQUEST_SOURCE, timestamp, _local_server_id, uuid, amount);
        StateMachine.DepositResponse response = (StateMachine.DepositResponse)executeRequest(request);
        return response.getStatus();
    }

    @Override
    public int getBalance(int uuid) throws IOException, InterruptedException, NotBoundException {
        int timestamp = _clock.advance();
        StateMachine.GetBalanceRequest request = new StateMachine.GetBalanceRequest(REQUEST_SOURCE, timestamp, _local_server_id, uuid);
        StateMachine.GetBalanceResponse response = (StateMachine.GetBalanceResponse)executeRequest(request);
        return response.getBalance();
    }

    @Override
    public Status transfer(int source_uuid, int target_uuid, int amount)
            throws IOException, InterruptedException, NotBoundException
    {
        int timestamp = _clock.advance();
        StateMachine.TransferRequest request = new StateMachine.TransferRequest(REQUEST_SOURCE, timestamp, _local_server_id, source_uuid, target_uuid, amount);
        StateMachine.TransferResponse response = (StateMachine.TransferResponse)executeRequest(request);
        return response.getStatus();
    }

    private StateMachine.Response executeRequest(StateMachine.Request request)
            throws IOException, NotBoundException, InterruptedException
    {
        _state_machine.enqueue(request);
        multicast(request);
        StateMachine.Response response = _state_machine.execute(request);
        multicast(peer -> peer.execute(request.getTimestamp(), request.getServerId()));
        return response;
    }

    @Override
    public void halt(int client_id) throws IOException, NotBoundException {
        if(!_halted_clients.contains(client_id))
            _halted_clients.add(client_id);
        if(_halted_clients.size() == _num_clients) {
            multicast(IBankServicePeer::halt);
            BankServer.shutdown();
        }
    }

    private void multicast(StateMachine.Request request) throws IOException, NotBoundException {
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
