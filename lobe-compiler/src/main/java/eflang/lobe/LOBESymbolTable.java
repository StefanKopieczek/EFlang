package eflang.lobe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

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
		return getNewInternalVariable(compiler, null);
	}	
	
	public Variable getNewInternalVariable(LOBECompiler compiler, Variable... siblings) {		
		String vName = getName();
		Variable var = new Variable(vName); 
		
		if (siblings != null && siblings.length > 0) {
			compiler.registerVariableNearSiblings(var, siblings);
		}
		else {			
			Integer idx = mMinInternalIdx;
			while (values().contains(idx)) {
				idx += 1;
			}
			this.put(var, idx);
		}
		mTempVars.add(var);
		
		compiler.mOutput += "ZERO " + var.getRef(compiler) + "\n";
		return var;
	}
	
	public String getName() {
		String name = "!" + UUID.randomUUID().toString();
		while (containsKey(new Variable(name))) {
			name = "!" + UUID.randomUUID().toString();
		}
		return name;
	}
	
	public Variable[] getNewInternalVariables(LOBECompiler compiler, int numVars, Variable... siblings) {
		ArrayList<Variable> variables = new ArrayList<Variable>();
		while (variables.size() < numVars) {			
			ArrayList<Variable> currentSiblings = new ArrayList<Variable>(Arrays.asList(siblings));			
			currentSiblings.addAll(variables);
			variables.add(getNewInternalVariable(compiler, currentSiblings.toArray(new Variable[currentSiblings.size()])));
		}
		
		return variables.toArray(new Variable[variables.size()]);
	}
	
	
	public boolean isInternalVariable(Variable var)
	{
		return var.mName.startsWith("!v");
	}
	
	public void addVariable(Variable var) {
		if (this.containsKey(var)) {
			throw new VariableAlreadyRegisteredException(var);
		}
		
		Integer idx = mMinInternalIdx;
		while (values().contains(idx)) {
			idx += 1;
		}		
		this.put(var, idx);		
	}
	
	public void addVariable(Variable var, LOBECompiler compiler, Variable... siblings) {
		if (siblings == null || siblings.length == 0) {
			addVariable(var);
			return;
		}
			
		if (this.containsKey(var)) {
			throw new VariableAlreadyRegisteredException(var);
		}
		
		compiler.registerVariableNearSiblings(var, siblings);
	}
	
	public void addVariable(Variable var, int cellIdx) {
		if (this.containsKey(var)) {
			throw new VariableAlreadyRegisteredException(var);
		}
		else if (values().contains(cellIdx)) {
			throw new CellAlreadyInUseException(cellIdx, getVarFromCell(cellIdx), var);
		}
		else {			
			this.put(var, cellIdx);
		}
	}
	
	public Variable getVarFromCell(int cellIdx) {
		for (Variable var : keySet()) {
			if (get(var).intValue() == cellIdx)
				return var;
		}
		
		return null;
	}
	
	public boolean isCellFree(int idx) {
		return !this.values().contains(idx);
	}
	
	public void deleteVariable(Variable var) {
		this.remove(var);		
	}
	
	public void deleteVariables(Variable... vars) {
		for (Variable var : vars) {
			deleteVariable(var);
		}
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
