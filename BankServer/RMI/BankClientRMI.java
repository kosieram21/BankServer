package BankServer.RMI;

import java.rmi.Naming;

public class BankClientRMI {
    public static void main(String args[]) throws Exception {
        if (args.length != 1) throw new RuntimeException("Syntax: DateClient <hostname>");
//        System.setSecurityManager(new SecurityManager());
        IBankServiceRMI bank_service = (IBankServiceRMI) Naming.lookup("//" + args[0] + "/BankServer.RMI.BankServiceRMI");
        int uuid = bank_service.createAccount();
        System.out.println(uuid);
    }
}
