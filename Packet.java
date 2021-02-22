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

        public RequestId getRequestId() { return getRequestIdFromBuffer(0); }
        public void setRequestId(RequestId val) { setRequestIdInBuffer(0, val); }

        protected RequestId getRequestIdFromBuffer(int pos) { return RequestId.convert(_buffer.get(pos)); }
        protected void setRequestIdInBuffer(int pos, RequestId val) { _buffer.put(pos, (byte)val.ordinal()); }

        protected int getIntFromBuffer(int pos) { return _buffer.getInt(pos); }
        protected void setIntInBuffer(int pos, int val) { _buffer.putInt(pos, val); }

        protected Status getStatusFromBuffer(int pos) { return Status.convert(_buffer.get(pos)); }
        protected void setStatusInBuffer(int pos, Status val) { _buffer.put(pos, (byte)val.ordinal()); }
    }

    // region Requests

    static final class CreateAccountRequest extends Base {
        CreateAccountRequest() {
            super(1);
            setRequestId(RequestId.createAccount);
        }
    }

    static final class DepositRequest extends Base {
        DepositRequest() {
            super(9);
            setRequestId(RequestId.deposit);
        }

        public int getUuid() { return getIntFromBuffer(1); }
        public void setUuid(int val) { setIntInBuffer(1, val); }

        public int getAmount() { return getIntFromBuffer(5); }
        public void setAmount(int val) { setIntInBuffer(5, val); }
    }

    static final class GetBalanceRequest extends Base {
        GetBalanceRequest() {
            super(5);
            setRequestId(RequestId.getBalance);
        }

        public int getUuid() { return getIntFromBuffer(1); }
        public void setUuid(int val) { setIntInBuffer(1, val); }
    }

    static final class TransferRequest extends Base {
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

    static final class CreateAccountResponse extends Base {
        CreateAccountResponse() {
            super(5);
            setRequestId(RequestId.createAccount);
        }

        public int getUuid() { return getIntFromBuffer(1); }
        public void setUuid(int val) { setIntInBuffer(1, val); }
    }

    static final class DepositResponse extends Base {
        DepositResponse() {
            super(2);
            setRequestId(RequestId.deposit);
        }

        public Status getStatus() { return getStatusFromBuffer(1); }
        public void setStatus(Status val) { setStatusInBuffer(1, val); }
    }

    static final class GetBalanceResponse extends Base {
        GetBalanceResponse() {
            super(5);
            setRequestId(RequestId.getBalance);
        }

        public int getAmount() { return getIntFromBuffer(1); }
        public void setAmount(int val) { setIntInBuffer(1, val); }
    }

    static final class TransferResponse extends Base {
        TransferResponse() {
            super(2);
            setRequestId(RequestId.transfer);
        }

        public Status getStatus() { return getStatusFromBuffer(1); }
        public void setStatus(Status val) { setStatusInBuffer(1, val); }
    }

    // endregion
}
