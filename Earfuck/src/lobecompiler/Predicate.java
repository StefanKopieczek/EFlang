package lobecompiler;

public enum Predicate {
	LT("<"),
	EQ("=="),
	NEQ("!=");
	
	private String mSymbol;
	
	Predicate(String symbol) {
		mSymbol = symbol;
	}
}
