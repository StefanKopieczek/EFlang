package lobecompiler;

public class InvalidOperationTokenException extends RuntimeException {
	public InvalidOperationTokenException() {
		super();
	}
	public InvalidOperationTokenException(String msg) {
		super(msg);
	}
}
