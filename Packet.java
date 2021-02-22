import java.nio.ByteBuffer;

public class Packet {
    enum RequestId {
        createAccount,
        deposit,
        getBalance,
        transfer;

        public static RequestId convert(byte val) {
            return RequestId.values()[val];
        }
    }

    abstract static class Base {
        private final ByteBuffer _buffer;

        Base(int buffSize) { _buffer = ByteBuffer.allocate(buffSize); }

        protected RequestId getRequestIdFromBuffer(int pos) { return RequestId.convert(_buffer.get(pos)); }
        protected void setRequestIdInBuffer(int pos, RequestId val) { _buffer.put(pos, (byte)val.ordinal()); }

        protected int getIntFromBuffer(int pos) { return _buffer.getInt(pos); }
        protected void setIntInBuffer(int pos, int val) { _buffer.putInt(pos, val); }

        protected Status getStatusFromBuffer(int pos) { return Status.convert(_buffer.get(pos)); }
        protected void setStatusInBuffer(int pos, Status val) { _buffer.put(pos, (byte)val.ordinal()); }
    }

    // region Requests

    static abstract class Request extends Base {
        Request(int buffSize) { super(buffSize); }

        public RequestId getRequestId() { return getRequestIdFromBuffer(0); }
        public void setRequestId(RequestId val) { setRequestIdInBuffer(0, val); }
    }

    static final class CreateAccountRequest extends Request {
        CreateAccountRequest() {
            super(1);
            setRequestId(RequestId.createAccount);
        }
    }

    static final class DepositRequest extends Request {
        DepositRequest() {
            super(9);
            setRequestId(RequestId.deposit);
        }

        public int getUuid() { return getIntFromBuffer(1); }
        public void setUuid(int val) { setIntInBuffer(1, val); }

        public int getAmount() { return getIntFromBuffer(5); }
        public void setAmount(int val) { setIntInBuffer(5, val); }
    }

    static final class GetBalanceRequest extends Request {
        GetBalanceRequest() {
            super(5);
            setRequestId(RequestId.getBalance);
        }

        public int getUuid() { return getIntFromBuffer(1); }
        public void setUuid(int val) { setIntInBuffer(1, val); }
    }

    static final class TransferRequest extends Request {
        TransferRequest() {
            super(13);
            setRequestId(RequestId.transfer);
        }

        public int getSourceUuid() { return getIntFromBuffer(1); }
        public void setSourceUuid(int val) { setIntInBuffer(1, val); }

        public int getTargetUuid() { return getIntFromBuffer(5); }
        public void setTargetUuid(int val) { setIntInBuffer(5, val); }

        public int getAmount() { return getIntFromBuffer(9); }
        public void setAmount(int val) { setIntInBuffer(9, val); }
    }

    // endregion

    // region Responses

    static abstract class Response extends Base {
        Response(int buffSize) { super(buffSize); }
    }

    static final class CreateAccountResponse extends Response {
        CreateAccountResponse() { super(4); }

        public int getUuid() { return getIntFromBuffer(0); }
        public void setUuid(int val) { setIntInBuffer(0, val); }
    }

    static final class DepositResponse extends Response {
        DepositResponse() { super(1); }

        public Status getStatus() { return getStatusFromBuffer(0); }
        public void setStatus(Status val) { setStatusInBuffer(0, val); }
    }

    static final class GetBalanceResponse extends Response {
        GetBalanceResponse() { super(4); }

        public int getAmount() { return getIntFromBuffer(0); }
        public void setAmount(int val) { setIntInBuffer(0, val); }
    }

    static final class TransferResponse extends Response {
        TransferResponse() { super(1); }

        public Status getStatus() { return getStatusFromBuffer(1); }
        public void setStatus(Status val) { setStatusInBuffer(1, val); }
    }

    // endregion
}
