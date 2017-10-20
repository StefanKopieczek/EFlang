package eflang.ear;

public class Instruction {
    public enum Type {
        GOTO, INCREMENT, DECREMENT, START_LOOP, END_LOOP, ENSURE_HAPPY, ENSURE_SAD, INPUT, OUTPUT
    }

    private Type type;
    private int value;

    private Instruction(Type type) {
        this(type, 0);
    }

    private Instruction(Type type, int value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public static Instruction goTo(int value) {
        return new Instruction(Type.GOTO, value);
    }

    public static Instruction increment() {
        return new Instruction(Type.INCREMENT);
    }

    public static Instruction decrement() {
        return new Instruction(Type.DECREMENT);
    }

    public static Instruction startLoop() {
        return new Instruction(Type.START_LOOP);
    }

    public static Instruction endLoop() {
        return new Instruction(Type.END_LOOP);
    }

    public static Instruction ensureHappy() {
        return new Instruction(Type.ENSURE_HAPPY);
    }

    public static Instruction ensureSad() {
        return new Instruction(Type.ENSURE_SAD);
    }

    public static Instruction input() {
        return new Instruction(Type.INPUT);
    }

    public static Instruction output() {
        return new Instruction(Type.OUTPUT);
    }
}
