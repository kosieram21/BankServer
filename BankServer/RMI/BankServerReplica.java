package BankServer.RMI;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class BankServerReplica {
    private final String _hostname;
    private final int _server_id;
    private final int _rmi_registry_port;

    private IBankServicePeer _peer;

    BankServerReplica(String hostname, int server_id, int rmi_registry_port) {
        _hostname = hostname;
        _server_id = server_id;
        _rmi_registry_port = rmi_registry_port;

        _peer = null;
    }

    public String getHostname() { return _hostname; }

    public int getServerId() { return _server_id; }

    public int getRmiRegistryPort() { return _rmi_registry_port; }

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
