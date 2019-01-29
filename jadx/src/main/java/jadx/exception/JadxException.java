package jadx.exception;

import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.ErrorsCounter;

public class JadxException extends Exception {

    public JadxException(String message) {
        super(message);
    }

    public JadxException(String message, Throwable cause) {
        super(message, cause);
    }

    public JadxException(ClassNode cls, String msg, Throwable th) {
        super(ErrorsCounter.formatErrorMsg(cls, msg), th);
    }

    public JadxException(MethodNode mth, String msg, Throwable th) {
        super(ErrorsCounter.formatErrorMsg(mth, msg), th);
    }

}
