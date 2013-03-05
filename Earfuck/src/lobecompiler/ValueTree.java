package lobecompiler;

public class ValueTree implements Evaluable {
	public Operator mOperator;	
	public Evaluable mLeft;
	public Evaluable mRight;	
	
	public ValueTree(Operator operator, 
			             Evaluable left, 
			             Evaluable right) {
		mOperator = operator;
		mLeft = left;
		mRight = right;		
	}
	
	public int getDepth() {
		return Math.max(mLeft.getDepth(), mRight.getDepth()) + 1;
	}
	
	public Value simplify(LOBECompiler compiler) {	
		Value leftVal = mLeft.simplify(compiler);
		Value rightVal = mRight.simplify(compiler);
		return compiler.evaluate(mOperator, leftVal, rightVal);
	}
}
