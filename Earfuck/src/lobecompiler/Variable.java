package lobecompiler;

public class Variable implements Value {
	
	private String mName;
	
	public Variable(String name) {
		mName = name;
	}
	
	public Value simplify(LOBECompiler compiler) {
		return this;
	}
	
	public String getEARReference(LOBECompiler compiler) {
		return compiler.getEARReference(this);
	}
}
