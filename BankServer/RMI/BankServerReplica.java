package BankServer.RMI;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class BankServerReplica {
    private final String _hostname;
    private final int _serverId;
    private final int _rmiRegistryPort;

    private IBankServicePeer _peer;

    BankServerReplica(String hostname, int serverId, int rmiRegistryPort) {
        _hostname = hostname;
        _serverId = serverId;
        _rmiRegistryPort = rmiRegistryPort;

        _peer = null;
    }

    public String getHostname() { return _hostname; }

    public int getServerId() { return _serverId; }

    public int getRmiRegistryPort() { return _rmiRegistryPort; }

    public int receiveRequest(RequestQueue.Request request) throws NotBoundException, IOException, InterruptedException {
        initializeBankServicePeerInterface();
        return request.sendToPeer(_peer);
    }

    public void executeRequest(int timestamp, int pId) throws NotBoundException, IOException, InterruptedException {
        initializeBankServicePeerInterface();
        _peer.execute(timestamp, pId);
    }

    private void initializeBankServicePeerInterface() throws NotBoundException, MalformedURLException, RemoteException {
        if(_peer == null) {
            final String bank_service_peer_name = "//" + getHostname() + ":" + getRmiRegistryPort() + "/" + ServiceNames.BANK_SERVICE_PEER;
            _peer = (IBankServicePeer) Naming.lookup(bank_service_peer_name);
        }
    }
}
