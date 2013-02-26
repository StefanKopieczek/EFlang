package lobecompiler;

public interface Evaluable {
	public Value simplify(LOBECompiler compiler);
	public int getDepth();
}
