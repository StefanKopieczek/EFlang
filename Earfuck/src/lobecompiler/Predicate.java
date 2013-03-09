package lobecompiler;

public enum Predicate {	
	EQ("=="),
	NEQ("!="),
	LEQ("<="),
	LT("<");
	
	public String mSymbol;
	
	Predicate(String symbol) {
		mSymbol = symbol;
	}
}
