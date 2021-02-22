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

    static class DepositRequest extends Base {
        DepositRequest() {
            super(9);
            setRequestId(RequestId.deposit);
        }

        public int getUuid() { return getIntFromBuffer(1); }
        public void setUuid(int val) { setIntInBuffer(1, val); }

        public int getAmount() { return getIntFromBuffer(5); }
        public void setAmount(int val) { setIntInBuffer(5, val); }
    }
}
