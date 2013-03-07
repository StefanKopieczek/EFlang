package lobecompiler;

public interface Evaluable {
	public Value evaluate(LOBECompiler compiler);
	public int getDepth();
}
