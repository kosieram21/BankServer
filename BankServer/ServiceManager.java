package BankServer;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public class ServiceManager {
    public static final String BANK_SERVICE = "BankServer.BankService";
    public static final String BANK_SERVICE_PEER = "BankServer.BankServicePeer";

    private final Hashtable<String, Remote> _services = new Hashtable<String, Remote>();
    private int _port;

    private static ServiceManager _instance;
    public static ServiceManager getInstance() {
        if (_instance == null)
            _instance = new ServiceManager();
        return _instance;
    }

    public void setPort(int port) {
        _port = port;
    }

    public <TService extends Remote> void bindService(Remote service, String service_name, int id)
            throws RemoteException, AlreadyBoundException
    {
        TService service_stub = (TService) UnicastRemoteObject.exportObject(service, 0);
        Registry registry = getRmiRegistry(_port);
        String full_service_name = fullServiceName(service_name, id);
        registry.bind(full_service_name, service_stub);
        _services.put(full_service_name, service);
    }

    public void unbindService(Remote service, String service_name, int id)
            throws RemoteException, NotBoundException
    {
        Registry registry = getRmiRegistry(_port);
        registry.unbind(fullServiceName(service_name, id));
        UnicastRemoteObject.unexportObject(service, true);
    }

    public void unbindAllServices(int id)
            throws RemoteException, NotBoundException
    {
        Set<String> keys = _services.keySet();
        for(String key : keys)
            unbindService(_services.get(key), key, id);
    }

    public <TService extends Remote> List<TService> getServices(ConfigFile config_file, String service_name)
            throws RemoteException, NotBoundException, MalformedURLException
    {
        List<TService> services = new ArrayList<TService>();
        for(ConfigFile.Entry entry : config_file)
            services.add(getService(entry, service_name));
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
