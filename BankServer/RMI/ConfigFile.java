package BankServer.RMI;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class ConfigFile implements Iterable<ConfigFile.Entry> {
    static class Entry {
        private final String _hostname;
        private final int _server_id;
        private final int _rmi_registry_port;

        Entry(String hostname, int server_id, int rmi_registry_port) {
            _hostname = hostname;
            _server_id = server_id;
            _rmi_registry_port = rmi_registry_port;
        }

        public String getHostname() { return _hostname; }

        public int getServerId() { return _server_id; }

        public int getRmiRegistryPort() { return _rmi_registry_port; }
    }

    private final List<Entry> _entries;

    ConfigFile(List<Entry> entries) { _entries = entries;}

    public static ConfigFile parse(String filepath) throws FileNotFoundException {
        File file = new File(filepath);
        Scanner scanner = new Scanner(file);

        List<Entry> entries = new ArrayList<Entry>();
        while(scanner.hasNext()) {
            String line = scanner.nextLine();
            String[] parts = line.split("-");

            String host_name = parts[0];
            int server_id = Integer.parseInt(parts[1]);
            int rmi_registry_port = Integer.parseInt(parts[2]);

            Entry entry = new Entry(host_name, server_id, rmi_registry_port);
            entries.add(entry);
        }

        scanner.close();
        return new ConfigFile(entries);
    }

    public Entry getEntry(int server_id) throws Exception {
        for(Entry entry : _entries) {
            if(entry.getServerId() == server_id)
                return entry;
        }
        throw new Exception("Server with id: " + String.valueOf(server_id) + " not found in config.");
    }

    public void removeEntry(int server_id) throws Exception {
        Entry entry = getEntry(server_id);
        _entries.remove(entry);
    }

    @Override
    public Iterator<Entry> iterator() {
        return _entries.iterator();
    }
}
