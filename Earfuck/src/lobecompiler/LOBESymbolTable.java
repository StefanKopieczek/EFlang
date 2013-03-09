package lobecompiler;

import java.util.HashMap;

public class LOBESymbolTable extends HashMap<Variable, Integer> {
	private Integer mLastInternalIdx;
	
	public LOBESymbolTable() {
		super();
		mLastInternalIdx = 10;		
	}
	
	public Variable getNewInternalVariable() {
		while (this.values().contains(mLastInternalIdx)) {
			mLastInternalIdx += 1;
		}
		String vName = "!v" + Integer.toString(mLastInternalIdx);
		Variable var = new Variable(vName); 
		this.put(var, mLastInternalIdx);
		return var;
	}
	
	public void addVariable(Variable var) {
		while (this.values().contains(mLastInternalIdx)) {
			mLastInternalIdx += 1;
		}
		this.put(var, mLastInternalIdx);		
	}
}
