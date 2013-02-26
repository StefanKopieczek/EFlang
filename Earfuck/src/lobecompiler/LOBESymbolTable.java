package lobecompiler;

import java.util.HashMap;

public class LOBESymbolTable extends HashMap<Variable, Integer> {
	private Integer mLastInternalIdx;
	
	public LOBESymbolTable() {
		super();
		mLastInternalIdx = 0;		
	}
	public Variable getNewInternalVariable() {
		mLastInternalIdx -= 1;
		while (this.values().contains(mLastInternalIdx)) {
			mLastInternalIdx -= 1;
		}
		String vName = "!v" + Integer.toString(mLastInternalIdx);
		Variable var = new Variable(vName); 
		this.put(var, mLastInternalIdx);
		return var;
	}
}
