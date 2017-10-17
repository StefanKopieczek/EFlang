package eflang.core;

public interface IoManager {
	public void requestInput(Parser parser);
	public void output(int value);
}
