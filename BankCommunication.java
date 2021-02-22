import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class BankCommunication {
    private final Socket _socket;
    protected final ObjectOutputStream _obj_out;
    protected final ObjectInputStream _obj_in;

    BankCommunication(Socket socket) throws IOException {
        _socket = socket;
        _obj_out = new ObjectOutputStream(_socket.getOutputStream());
        _obj_in  = new ObjectInputStream(_socket.getInputStream());
    }

    public void close() throws IOException {
        _obj_in.close();
        _obj_out.close();
        _socket.close();
    }
}
