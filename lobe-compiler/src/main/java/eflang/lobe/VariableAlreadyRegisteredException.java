package eflang.lobe;

public class VariableAlreadyRegisteredException extends RuntimeException {
    public VariableAlreadyRegisteredException(Variable v) {
        super(v.mName);
    }
}
