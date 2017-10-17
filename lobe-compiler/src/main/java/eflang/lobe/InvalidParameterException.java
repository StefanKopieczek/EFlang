package eflang.lobe;

public class InvalidParameterException extends LobeCompilationException {
	public InvalidParameterException() {
		super();
	}
	public InvalidParameterException(String msg) {
		super(msg);
	}
}
