package lobecompiler;

import java.util.ArrayList;
import java.util.HashMap;

public class LOBESymbolTable extends HashMap<Variable, Integer> {
	private Integer mMinInternalIdx;
	private ArrayList<Variable> mTempVars;
	private ArrayList<Variable> mLockedVars;
	
	public LOBESymbolTable() {
		super();
		mMinInternalIdx = 1;		
		mTempVars = new ArrayList<Variable>();
		mLockedVars = new ArrayList<Variable>();
	}
	
	public Variable getNewInternalVariable(LOBECompiler compiler) {
		Integer idx = mMinInternalIdx;
		while (this.values().contains(idx)) {
			idx += 1;
		}
		String vName = "!v" + Integer.toString(idx);
		Variable var = new Variable(vName); 
		this.put(var, idx);
		mTempVars.add(var);
		compiler.mOutput += "ZERO " + var.getRef(compiler) + "\n";
		return var;
	}
	
	public void addVariable(Variable var) {
		Integer idx = mMinInternalIdx;
		while (this.values().contains(idx)) {
			idx += 1;
		}
		this.put(var, idx);		
	}
	
	public void deleteVariable(Variable var) {
		this.remove(var);
	}
	
	public void lockVariable(Variable var) {
		mLockedVars.add(var);
	}
	
	public void unlockVariable(Variable var) {
		mLockedVars.remove(var);
	}
	
	public void clearInternalVars() {
		ArrayList<Variable> doomedVars = new ArrayList<Variable>();
		for (Variable var : mTempVars) {
			if (!mLockedVars.contains(var)){
				deleteVariable(var);
				doomedVars.add(var);
			}
		}
		for (Variable doomed : doomedVars) {
			mTempVars.remove(doomed);
		}
	}
}
