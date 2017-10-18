package eflang.core;

public interface IoManager {
    void requestInput(Parser parser);
    void output(int value);
}
