package lobecompiler;

public enum Predicate {	
	EQ("=="),
	NEQ("!="),
	LEQ("<="),
	LT("<"),
	GT(">"),
	GEQ(">=");
	
	public String mSymbol;
	
	Predicate(String symbol) {
		mSymbol = symbol;
	}
}
