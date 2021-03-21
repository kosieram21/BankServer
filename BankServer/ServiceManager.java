package BankServer;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ServiceManager {
    public static final String BANK_SERVICE = "BankServer.BankService";
    public static final String BANK_SERVICE_PEER = "BankServer.BankServicePeer";

    private final Hashtable<String, Remote> _services = new Hashtable<String, Remote>();
    private int _port;
    private int _id;

    private static ServiceManager _instance;
    public static ServiceManager getInstance() {
        if (_instance == null)
            _instance = new ServiceManager();
        return _instance;
    }

    public void setPort(int port) { _port = port; }
    public void setId(int id) { _id = id; }

    public <TService extends Remote> void bindService(String service_name, Remote service)
            throws RemoteException, AlreadyBoundException
    {
        TService service_stub = (TService) UnicastRemoteObject.exportObject(service, 0);
        Registry registry = getRmiRegistry(_port);
        String full_service_name = fullServiceName(service_name, _id);
        registry.bind(full_service_name, service_stub);
        _services.put(full_service_name, service);
    }

    public void unbindService(String service_name)
            throws RemoteException, NotBoundException
    {
        Remote service = _services.get(service_name);
        Registry registry = getRmiRegistry(_port);
        UnicastRemoteObject.unexportObject(service, true);
        registry.unbind(service_name);
    }

    public void unbindAllServices()
            throws RemoteException, NotBoundException
    {
        Set<String> keys = _services.keySet();
        for(String key : keys)
            unbindService(key);
    }

    public <TService extends Remote> HashMap<Integer, TService> getServices(ConfigFile config_file, String service_name)
            throws RemoteException, NotBoundException, MalformedURLException
    {
        HashMap<Integer, TService> services = new HashMap<Integer, TService>();
        for(ConfigFile.Entry entry : config_file)
            services.put(entry.getServerId(), getService(entry, service_name));
        return services;
    }

    public <TService extends Remote> TService getService(ConfigFile.Entry entry, String service_name)
            throws RemoteException, NotBoundException, MalformedURLException
    {
        final String service_path = getServicePath(entry.getHostname(), entry.getRmiRegistryPort(), service_name, entry.getServerId());
        return (TService) Naming.lookup(service_path);
    }

    private String getServicePath(String hostname, int port, String service, int id) {
        return String.format("//%s:%d/%s", hostname, port, fullServiceName(service, id));
    }

    private String fullServiceName(String service, int id) {
        return String.format("%s%d", service, id);
    }

    private Registry getRmiRegistry(int port) throws RemoteException {
        Registry registry;
        try {
            LocateRegistry.createRegistry(port);
            registry = LocateRegistry.getRegistry(port);
        }
        catch (RemoteException e) {
            registry = LocateRegistry.getRegistry(port);
        }
        return registry;
    }
}
