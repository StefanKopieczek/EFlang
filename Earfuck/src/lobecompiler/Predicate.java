package lobecompiler;

public enum Predicate {	
	EQ("=="),
	NEQ("!="),
	LEQ("<="),
	LT("<");
	
	private String mSymbol;
	
	Predicate(String symbol) {
		mSymbol = symbol;
	}
}
