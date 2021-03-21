package BankServer;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ServiceManager {
    public static final String BANK_SERVICE = "BankServer.BankService";
    public static final String BANK_SERVICE_PEER = "BankServer.BankServicePeer";

    public static <TService extends Remote> void bindService(Remote service, String service_name, int id, int port)
            throws RemoteException, AlreadyBoundException
    {
        TService service_stub = (TService) UnicastRemoteObject.exportObject(service, 0);
        Registry registry = getRmiRegistry(port);
        registry.bind(fullServiceName(service_name, id), service_stub);
    }

    public static <TService extends Remote> List<TService> getServices(ConfigFile config_file, String service_name)
            throws RemoteException, NotBoundException, MalformedURLException
    {
        List<TService> services = new ArrayList<TService>();
        for(ConfigFile.Entry entry : config_file)
            services.add(getService(entry, service_name));
        return services;
    }

    public static <TService extends Remote> TService getService(ConfigFile.Entry entry, String service_name)
            throws RemoteException, NotBoundException, MalformedURLException
    {
        final String service_path = getServicePath(entry.getHostname(), entry.getRmiRegistryPort(), service_name, entry.getServerId());
        return (TService) Naming.lookup(service_path);
    }

    private static String getServicePath(String hostname, int port, String service, int id) {
        return String.format("//%s:%d/%s", hostname, port, fullServiceName(service, id));
    }

    private static String fullServiceName(String service, int id) {
        return String.format("%s%d", service, id);
    }

    private static Registry getRmiRegistry(int port) throws RemoteException {
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
