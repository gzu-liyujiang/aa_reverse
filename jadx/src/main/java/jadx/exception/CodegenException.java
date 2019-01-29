package jadx.exception;

import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;

public class CodegenException extends JadxException {

    public CodegenException(String message) {
        super(message);
    }

    public CodegenException(String message, Throwable cause) {
        super(message, cause);
    }

    public CodegenException(ClassNode mth, String msg) {
        super(mth, msg, null);
    }

    public CodegenException(ClassNode mth, String msg, Throwable th) {
        super(mth, msg, th);
    }

    public CodegenException(MethodNode mth, String msg) {
        super(mth, msg, null);
    }

    public CodegenException(MethodNode mth, String msg, Throwable th) {
        super(mth, msg, th);
    }

}
