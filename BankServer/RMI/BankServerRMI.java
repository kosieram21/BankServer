package BankServer.RMI;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.registry.*;

public class BankServerRMI {
    public static void main(String[] args) throws Exception {
//        if (System.getSecurityManager() == null) {
//            System.setProperty("java.security.policy","file:./security.policy");
//            System.setSecurityManager(new SecurityManager());
//        }

        BankServiceRMI bank_service = new BankServiceRMI();
        IBankServiceRMI bank_service_stub = (IBankServiceRMI)UnicastRemoteObject.exportObject(bank_service, 0);

        final String bank_service_name = "BankServer.RMI.BankServiceRMI";
        if(args.length == 0) {
            Naming.bind(bank_service_name, bank_service_stub);
        }
        else {
            int port = Integer.parseInt(args[0]);
            Registry localRegistry = LocateRegistry.getRegistry(port);
            localRegistry.bind(bank_service_name, bank_service_stub);
        }
    }
}
