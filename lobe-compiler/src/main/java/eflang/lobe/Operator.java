package eflang.lobe;

public enum Operator {
    ADD('+'),
    SUB('-'),
    MUL('*'),
    DIV('/');

    public char mSymbol;

    private Operator(char symbol) {
        mSymbol = symbol;
    }

}
