package eflang.lobe;

public interface Evaluable {
    public Value evaluate(LOBECompiler compiler) throws LobeCompilationException;
    public Variable evaluate(LOBECompiler compiler, Variable target) throws LobeCompilationException;
    public int getDepth();
}
