package lobecompiler;

public class Constant implements Value {
	private int mValue;
	
	public Constant(int value) {
		mValue = value;
	}
	
	public int getValue() {
		return mValue;
	}
	
	public int getDepth() {
		return 1;
	}
	
	public Value simplify(LOBECompiler compiler) {
		return this;
	}
	
	public String getEARReference(LOBECompiler compiler) {
		return compiler.getEARReference(this);
	}
}
