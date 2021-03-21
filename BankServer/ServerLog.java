package BankServer;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerLog {
    private final Logger _logger = Logger.getLogger(ServerLog.class.getName());

    private static ServerLog _instance;
    public synchronized static ServerLog getInstance() {
        if (_instance == null)
            _instance = new ServerLog();
        return _instance;
    }

    private boolean _initialized = false;
    public void initialize(int server_id) throws IOException {
        if(!_initialized) {
            FileHandler handler = new FileHandler(String.format("serverLogfile-%d.txt", server_id));
            _logger.addHandler(handler);
            handler.setFormatter(new SimpleFormatter());
            _initialized = true;
        }
    }

    public void log(String msg) { _logger.info(msg); }
}
