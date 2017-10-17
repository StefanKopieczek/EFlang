package eflang.lobe;

public enum Predicate {
	EQ("=="),
	NEQ("!="),
	LEQ("<="),
	LT("<"),
	GEQ(">="),
	GT(">");

	public String mSymbol;

	Predicate(String symbol) {
		mSymbol = symbol;
	}
}
