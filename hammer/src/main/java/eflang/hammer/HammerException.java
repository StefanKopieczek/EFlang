package eflang.hammer;

public class HammerException extends RuntimeException {
    public HammerException(String message) {
        super(message);
    }

    public HammerException(String message, Exception cause) {
        super(message, cause);
    }
}
