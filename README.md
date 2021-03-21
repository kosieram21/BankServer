# Authors

Mitch Kosieradzki (kosie011), Shane Kosieradzki (kosie013)

# Compilation

From project root call the following command to compile project:

    javac -d bin BankServer/*.java

The remaining sections assume that you are making calls from within the `bin` directory created during compilation

# RMI implementation

For **Part B** run the following commands in order:

    java BankServer.Server <port>
    java BankServer.Client <host> <port> <numThreads> <iterationCount>
