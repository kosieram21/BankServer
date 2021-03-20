package BankServer.RMI;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class BankServerReplica {
    private final String _hostname;
    private final int _serverId;
    private final int _rmiRegistryPort;
    private IBankServicePeer _bank_service_peer;

    BankServerReplica(String hostname, int serverId, int rmiRegistryPort) {
        _hostname = hostname;
        _serverId = serverId;
        _rmiRegistryPort = rmiRegistryPort;
        _bank_service_peer = null;
    }

    public String getHostname() { return _hostname; }

    public int getServerId() { return _serverId; }

    public int getRmiRegistryPort() { return _rmiRegistryPort; }

    public IBankServicePeer getBankServicePeerInterface() throws NotBoundException, MalformedURLException, RemoteException {
        if(_bank_service_peer == null)
            _bank_service_peer = initializeBankServicePeerInterface();
        return _bank_service_peer;
    }

    public IBankServicePeer initializeBankServicePeerInterface() throws NotBoundException, MalformedURLException, RemoteException {
        final String bank_service_peer_name = "//" + _hostname + ":" + _rmiRegistryPort + "/" + ServiceNames.BANK_SERVICE_PEER;
        return (IBankServicePeer) Naming.lookup(bank_service_peer_name);
    }
}
