package BankServer;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogFile {
    static abstract class Base {
        private final Logger _logger;

        Base(String name) { _logger = Logger.getLogger(name); }

        private boolean _initialized = false;
        public void initialize(int id) throws IOException {
            if(!_initialized) {
                FileHandler handler = new FileHandler(String.format("%s-%d.txt", _logger.getName(), id));
                _logger.addHandler(handler);
                handler.setFormatter(new SimpleFormatter());
                _initialized = true;
            }
        }

        public void log(String msg) { _logger.info(msg); }
    }

    static class Server extends Base {
        Server() { super(LogFile.Server.class.getName()); }

        private static LogFile.Server _instance;
        public static LogFile.Server getInstance() {
            if (_instance == null)
                _instance = new LogFile.Server();
            return _instance;
        }
    }

    static class Client extends Base {
        Client() { super(LogFile.Client.class.getName()); }

        private static LogFile.Client _instance;
        public static LogFile.Client getInstance() {
            if (_instance == null)
                _instance = new LogFile.Client();
            return _instance;
        }
    }
}
