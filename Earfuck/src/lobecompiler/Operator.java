package lobecompiler;

public enum Operator {
	ADD('+'),
	SUB('-'),
	MUL('*');
	
	public char mSymbol;

	private Operator(char symbol) {
		mSymbol = symbol;
	}
	
}
