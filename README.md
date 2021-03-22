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

# Performance Experiment

## 5 servers:

- 9536336.98 ns
- 9927802.12 ns
- 8608254.38 ns

## 3 servers:

- 3550965.57 ns
- 4803238.95 ns
- 4167864.59 ns

## 1 server

- 398912.33 ns
- 401581.45 ns
- 426756.61 ns