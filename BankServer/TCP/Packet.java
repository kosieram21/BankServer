package BankServer.TCP;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import BankServer.Status;

public class Packet {
    enum RequestId {
        createAccount,
        deposit,
        getBalance,
        transfer,
        exit;

        public static RequestId convert(byte val) {
            return RequestId.values()[val];
        }
    }

    abstract static class Base implements Serializable {
        protected ByteBuffer _buffer;

        Base(int length) {
            _buffer = ByteBuffer.allocate(length);
            setByteInBuffer(0, (byte)(length - 1));
        }

        public byte getLength() { return getByteFromBuffer(0); }

        protected byte getByteFromBuffer(int pos) { return _buffer.get(pos); }
        protected void setByteInBuffer(int pos, byte val) { _buffer.put(pos, val); }

        protected int getIntFromBuffer(int pos) { return _buffer.getInt(pos); }
        protected void setIntInBuffer(int pos, int val) { _buffer.putInt(pos, val); }

        private void writeObject(java.io.ObjectOutputStream out) throws IOException {
            byte[] bytes = _buffer.array();
            out.write(bytes);
        }

        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
            byte length = in.readByte();
            byte[] bytes = new byte[length];
            int bytesRead = in.read(bytes, 0, length);

            if (bytesRead != length) throw new IOException();

            _buffer = ByteBuffer.allocate(length + 1);
            _buffer.put(0, length);
            for(int i = 0; i < length; i++)
                _buffer.put(i + 1, bytes[i]);
        }
    }

    // region Requests

    static abstract class Request extends Base {
        Request(int buffSize) { super(buffSize); }

        public RequestId getRequestId() { return getRequestIdFromBuffer(1); }
        public void setRequestId(RequestId val) { setRequestIdInBuffer(1, val); }

        protected RequestId getRequestIdFromBuffer(int pos) { return RequestId.convert(_buffer.get(pos)); }
        protected void setRequestIdInBuffer(int pos, RequestId val) { _buffer.put(pos, (byte)val.ordinal()); }
    }

    static final class CreateAccountRequest extends Request {
        CreateAccountRequest() {
            super(2);
            setRequestId(RequestId.createAccount);
        }
    }

    static final class DepositRequest extends Request {
        DepositRequest() {
            super(10);
            setRequestId(RequestId.deposit);
        }

        public int getUuid() { return getIntFromBuffer(2); }
        public void setUuid(int val) { setIntInBuffer(2, val); }

        public int getAmount() { return getIntFromBuffer(6); }
        public void setAmount(int val) { setIntInBuffer(6, val); }
    }

    static final class GetBalanceRequest extends Request {
        GetBalanceRequest() {
            super(6);
            setRequestId(RequestId.getBalance);
        }

        public int getUuid() { return getIntFromBuffer(2); }
        public void setUuid(int val) { setIntInBuffer(2, val); }
    }

    static final class TransferRequest extends Request {
        TransferRequest() {
            super(14);
            setRequestId(RequestId.transfer);
        }

        public int getSourceUuid() { return getIntFromBuffer(2); }
        public void setSourceUuid(int val) { setIntInBuffer(2, val); }

        public int getTargetUuid() { return getIntFromBuffer(6); }
        public void setTargetUuid(int val) { setIntInBuffer(6, val); }

        public int getAmount() { return getIntFromBuffer(10); }
        public void setAmount(int val) { setIntInBuffer(10, val); }
    }

    static final class ExitRequest extends Request {
        ExitRequest() {
            super(2);
            setRequestId(RequestId.exit);
        }
    }

    // endregion

    // region Responses

    static abstract class Response extends Base {
        Response(int buffSize) { super(buffSize); }

        protected Status getStatusFromBuffer(int pos) { return Status.convert(_buffer.get(pos)); }
        protected void setStatusInBuffer(int pos, Status val) { _buffer.put(pos, (byte)val.ordinal()); }
    }

    static final class CreateAccountResponse extends Response {
        CreateAccountResponse() { super(5); }

        public int getUuid() { return getIntFromBuffer(1); }
        public void setUuid(int val) { setIntInBuffer(1, val); }
    }

    static final class DepositResponse extends Response {
        DepositResponse() { super(2); }

        public Status getStatus() { return getStatusFromBuffer(1); }
        public void setStatus(Status val) { setStatusInBuffer(1, val); }
    }

    static final class GetBalanceResponse extends Response {
        GetBalanceResponse() { super(5); }

        public int getAmount() { return getIntFromBuffer(1); }
        public void setAmount(int val) { setIntInBuffer(1, val); }
    }

    static final class TransferResponse extends Response {
        TransferResponse() { super(2); }

        public Status getStatus() { return getStatusFromBuffer(1); }
        public void setStatus(Status val) { setStatusInBuffer(1, val); }
    }

    // endregion
}
