package lobecompiler;

public class Conditional {
	Predicate mPredicate;
	Evaluable mLeft;
	Evaluable mRight;
	
	public Conditional(Predicate predicate, Evaluable arg1, Evaluable arg2) {
		mPredicate = predicate;
		mLeft = arg1;
		mRight = arg2;		
	}	
}