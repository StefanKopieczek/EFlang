package lobecompiler;

public class LOBECompiler {
		
	private LOBESymbolTable mSymbols;
	
	public LOBECompiler() {
		mSymbols = new LOBESymbolTable();
	}
	
	public String getEARReference(Constant c) {
		return Integer.toString(c.getValue());
	}
	
	public String getEARReference(Variable v) {
		return "@" + Integer.toString(getPointer(v));
	}
	
	public int getPointer(Variable v) {
		return 0; // TODO
	}
	
	public Value evaluate(Operator op, Value val1, Value val2) {
		return new Constant(0); // TODO;
	}
}
