package jadx.exception;

import jadx.core.dex.nodes.MethodNode;

public class DecodeException extends JadxException {

    public DecodeException(String message) {
        super(message);
    }

    public DecodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecodeException(MethodNode mth, String msg) {
        super(mth, msg, null);
    }

    public DecodeException(MethodNode mth, String msg, Throwable th) {
        super(mth, msg, th);
    }

}
