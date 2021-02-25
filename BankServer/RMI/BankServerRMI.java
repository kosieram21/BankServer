package BankServer.RMI;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.registry.*;

public class BankServerRMI {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new RuntimeException("Syntax: tcp.BankServer port-number");
        final int port = Integer.parseInt(args[0]);

        BankServiceRMI bank_service = new BankServiceRMI();
        IBankServiceRMI bank_service_stub = (IBankServiceRMI)UnicastRemoteObject.exportObject(bank_service, 0);

        final String bank_service_name = "BankServer.RMI.BankServiceRMI";
        Naming.bind(bank_service_name, bank_service_stub);
        //Registry localRegistry = LocateRegistry.getRegistry(port);
        //localRegistry.bind(bank_service_name, bank_service_stub);
    }
}
