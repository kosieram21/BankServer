# USING 1 GRACE DAY
# Authors
- Mitch Kosieradzki (kosie011)
- Shane Kosieradzki (kosie013)

# Compilation
You can clone and build this project using the following commands:

    javac -d bin BankServer/*.java

The remaining sections assume that you are making calls from within the `bin` directory created during compilation

# Creating Processes
Bellow are instructions on how to create client/server processes. 
All server processes in the group **MUST** be created before clients attempt to connect.

See `config.txt` for an example configuration file.

## Server
The following command can be used to create a server-process:

    java BankServer.Server <server-id> <num-clients> <config-file-path>

## Client
The following command can be used to create a client-process:

    java BankServer.Client <client-id> <num-threads> <config-file-path>

## Configuration File
The configuration file defines the server replicas participating in the process group.
Each line of the configuration should be formatted as follows

    <hostname> <server-id> <rmi-port>

# Logging
A log file is generated for both the server and client processes.
They use the following naming scheme:

    BankServer.LogFile$Client-<client-id>.txt
    BankServer.LogFile$Server-<server-id>.txt


# Performance Experiment
The performance of this server was measured empirically with varying degrees of replication.
In our test each client-process utilized 24 threads, each of which performed 200 transfer-requests against a random server.
We varied the number of servers in the replica group and measured the average time it took each client-process to make a transfer-request.
We used 3 client processes to generate the load applied to the replica-group.
The results of our experimentations our recorded bellow.

## 5 servers
- `csel-kh4250-06: 9536336.98 ns`
- `csel-kh4250-07: 9927802.12 ns`
- `csel-kh4250-08: 8608254.38 ns`

## 3 servers
- `csel-kh4250-06: 3550965.57 ns`
- `csel-kh4250-07: 4803238.95 ns`
- `csel-kh4250-08: 4167864.59 ns`

## 1 server
- `csel-kh4250-06: 398912.33 ns`
- `csel-kh4250-07: 401581.45 ns`
- `csel-kh4250-08: 426756.61 ns`

## Lab Machines Used
We used the following resources to preform our experiments.

### Servers Used
- `csel-kh4250-01.cselabs.umn.edu`
- `csel-kh4250-02.cselabs.umn.edu` 
- `csel-kh4250-03.cselabs.umn.edu`
- `csel-kh4250-04.cselabs.umn.edu`
- `csel-kh4250-05.cselabs.umn.edu`

### Clients Used
- `csel-kh4250-06.cselabs.umn.edu`
- `csel-kh4250-07.cselabs.umn.edu`
- `csel-kh4250-08.cselabs.umn.edu`
