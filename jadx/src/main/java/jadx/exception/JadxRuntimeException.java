package jadx.exception;

public class JadxRuntimeException extends RuntimeException {

    public JadxRuntimeException(String message) {
        super(message);
    }

    public JadxRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
