package lobecompiler;

public class Variable implements Value {
	
	public String mName;
	
	public Variable(String name) {
		mName = name;
	}
	
	@Override
	public int hashCode() {
		return mName.hashCode();
	}
	
	@Override 
	public String toString() {
		return mName;
	}
	
	@Override public boolean equals(Object obj) {
		 if (obj == null)
	            return false;
	        if (obj == this)
	            return true;
	        if (obj.getClass() != getClass())
	            return false;
	        return obj.hashCode() == hashCode();
	}
	
	public int getDepth() {
		return 1;
	}
	
	public Value evaluate(LOBECompiler compiler) {
		return this;
	}
	
	public Variable evaluate(LOBECompiler compiler, Variable target) {
	    compiler.storeValue(this, target);
	    return target;
	}
	
	public String getRef(LOBECompiler compiler) {		
		return compiler.getRef(this);
	}
}
