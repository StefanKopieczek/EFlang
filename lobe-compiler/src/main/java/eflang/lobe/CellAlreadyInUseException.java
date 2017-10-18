package eflang.lobe;

public class CellAlreadyInUseException extends RuntimeException {
    public CellAlreadyInUseException(int cellIdx, Variable oldV, Variable newV) {
        super("Cannot register " + newV.mName + " in cell " +
              cellIdx + " as is used by " + oldV.mName);
    }
}
