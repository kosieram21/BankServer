import java.rmi.server.UnicastRemoteObject;
import java.rmi.RMISecurityManager;
import java.rmi.Naming;
import java.rmi.registry.*;

public class BankServerRMI {
    public static void main(String[] args) throws Exception {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());

        BankServiceRMI bankService = new BankServiceRMI();
        IBankServiceRMI bankServiceStub = (IBankServiceRMI)UnicastRemoteObject.exportObject(bankService, 0);

        final String bank_service_name = "BankService";
        if(args.length == 0) {
            Naming.bind(bank_service_name, bankServiceStub);
        }
        else {
            int port = Integer.parseInt(args[0]);
            Registry localRegistry = LocateRegistry.getRegistry(port);
            localRegistry.bind(bank_service_name, bankServiceStub);
        }
    }
}
