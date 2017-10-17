package eflang.lobe;

public class Conditional implements Evaluable {
	Predicate mPredicate;
	Evaluable mLeft;
	Evaluable mRight;
	
	public Conditional(Predicate predicate, Evaluable arg1, Evaluable arg2) {
		mPredicate = predicate;
		mLeft = arg1;
		mRight = arg2;		
	}	
	
	public Variable evaluate(LOBECompiler compiler)
	    throws LobeCompilationException 
	{
	    return evaluate(compiler, compiler.getNewInternalVariable());
	}
	
	public Variable evaluate(LOBECompiler compiler, Variable target) 	
	    throws LobeCompilationException 
	{
		Value left = mLeft.evaluate(compiler);
		Value right = mRight.evaluate(compiler);
		return compiler.evaluate(mPredicate, left, right, target);
	}	
	
	public int getDepth() {
		return 1 + Math.max(mLeft.getDepth(), mRight.getDepth());
	}
}