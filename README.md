# Authors

Mitch Kosieradzki (kosie011), Shane Kosieradzki (kosie013)

# Compilation

From project root call the following commands to compile project:

    javac -d bin BankServer/*.java
    javac -d bin BankServer/RMI/*.java
    javac -d bin BankServer/TCP/*.java

The remaining sections assume that you are making calls from within the `bin` directory created during compilation

# TCP implementation

For **Part A** run the following commands in order:

    java BankServer.TCP.BankServer <port>
    java BankServer.TCP.BankClient <host> <port> <numThreads> <iterationCount>


# RMI implementation

For **Part B** run the following commands in order:

    java BankServer.RMI.BankServer <port>
    java BankServer.RMI.BankClient <host> <port> <numThreads> <iterationCount>
