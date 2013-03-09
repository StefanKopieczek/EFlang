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
	
	public Value evaluate(LOBECompiler compiler) {
		return this;
	}
	
	public String getRef(LOBECompiler compiler) {
		return compiler.getRef(this);
	}
}
