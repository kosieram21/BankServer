package BankServer.RMI;

public class BankServerReplica {
    private final String _hostname;
    private final int _serverId;
    private final int _rmiRegistryPort;

    BankServerReplica(String hostname, int serverId, int rmiRegistryPort) {
        _hostname = hostname;
        _serverId = serverId;
        _rmiRegistryPort = rmiRegistryPort;
    }

    public String getHostname() {
        return _hostname;
    }

    public int getServerId() {
        return _serverId;
    }

    public int getRmiRegistryPort() {
        return _rmiRegistryPort;
    }
}
