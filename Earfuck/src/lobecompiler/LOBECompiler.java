package lobecompiler;

import earcompiler.EARCompiler;

public class LOBECompiler {
		
	private LOBESymbolTable mSymbols;
	private EARCompiler earCompiler;
	private String mOutput;	
	
	public LOBECompiler() {
		mSymbols = new LOBESymbolTable();
		initWorkingMemory();
		mOutput = "";
	}
	
	private void initWorkingMemory() {
		mSymbols.put(new Variable("!w0"), 0);
		mSymbols.put(new Variable("!w1"), 1);
		mSymbols.put(new Variable("!w2"), 2);
	}
	
	public String getEARReference(Constant c) {
		return Integer.toString(c.getValue());
	}
	
	public String getEARReference(Variable v) {
		return "@" + Integer.toString(getPointer(v));
	}
	
	public int getPointer(Variable v) {
		return mSymbols.get(v);
	}
	
	public Value evaluate(Operator op, Value val1, Value val2) {
		Value result;
		String arg1Name = val1.getEARReference(this);
		String arg2Name = val2.getEARReference(this);
		String opName;
		
		String earCommand = "";
		if (val1 instanceof Variable) {
			earCommand += "COPY " + arg1Name + " " + mSymbols.get("!w0") + ";";
			arg1Name = "@0";
		}
		if (val2 instanceof Variable) {
			earCommand += "COPY " + arg1Name + " " + mSymbols.get("!w1") + ";";
			arg2Name = "@1";
		}
		
		if (op == op.ADD) {
			opName = "ADD";
		}
		else if (op == op.MUL) {
			opName = "MUL";
		}
		else if (op == op.SUB) {
			opName = "SUB";
		}
		else {
			throw new InvalidOperationTokenException(
					                   "Unknown operation token " + op.name());
		}					
		
		Variable targetVar = mSymbols.getNewInternalVariable();
		String targetVarName = targetVar.getEARReference(this);
		
		if (op != op.MUL){ 
			earCommand += opName + " " + arg1Name + " " + arg2Name + "; ";
			earCommand += "MOV " + arg2Name + " " + targetVarName + " !w2;";
		}
		else {
			earCommand += opName + " " + 
		                  arg1Name + " " + 
					      arg2Name + " " +
		                  targetVarName + " " +
					      "!w2;";
		}
		mOutput += earCompiler.compile(earCommand);
		
		return targetVar; // TODO;
	}
	
	public Variable evaluate(Predicate pred, Value val1, Value val2) {
		Value result;
		String arg1Name = val1.getEARReference(this);
		String arg2Name = val2.getEARReference(this);
		String resultCell;
		
		String earCommand = "";		
		earCommand += "COPY " + arg1Name + " [[0]]; ";		
		earCommand += "COPY " + arg2Name + " [[1]]; ";		
		
		if (pred == Predicate.EQ) {
			earCommand += 
              "MOV 1 [[4]]; " +
			  "WHILE [[0]]; " +
              "MOV 0 [[4]]; " +
              "COPY [[1]] [[2]] [[5]]; " +
              "MOV 1 [[3]]; " +
              "WHILE [[2]]; " +
              "MOV 0 [[3]]; " +
              "MOV 1 [[4]]; " +
              "SUB 1 [[0]] [[1]]; " +
              "ZERO [[2]]; " +
              "ENDWHILE; " +
              "WHILE [[3]]; " +
              "ZERO [[0]]; " +
              "MOV 1 [[1]]; " +
              "ZERO [[3]]; " +
              "ENDWHILE; " +
              "ENDWHILE; " +
              "WHILE [[1]]; " +
              "MOV 0 [[4]]; " +
              "ZERO [[1]]; " +
              "ENDWHILE; ";
			resultCell = "[[4]]";			  
		}
		else if (pred == Predicate.NEQ) {
			earCommand += 
              "MOV 0 [[4]]; " +
			  "WHILE [[0]]; " +
              "MOV 1 [[4]]; " +
              "COPY [[1]] [[2]] [[5]]; " +
              "MOV 1 [[3]]; " +
              "WHILE [[2]]; " +
              "MOV 0 [[3]]; " +
              "MOV 0 [[4]]; " +
              "SUB 1 [[0]] [[1]]; " +
              "ZERO [[2]]; " +
              "ENDWHILE; " +
              "WHILE [[3]]; " +
              "ZERO [[0]]; " +
              "MOV 1 [[1]]; " +
              "ZERO [[3]]; " +
              "ENDWHILE; " +
              "ENDWHILE; " +
              "WHILE [[1]]; " +
              "MOV 1 [[4]]; " +
              "ZERO [[1]]; " +
              "ENDWHILE; ";
			resultCell = "[[4]]";		
		}
		else if (pred == Predicate.LEQ) {
			earCommand += 
              "MOV 1 [[4]]; " +
			  "WHILE [[0]]; " +
              "MOV 0 [[4]]; " +
              "COPY [[1]] [[2]] [[5]]; " +
              "MOV 1 [[3]]; " +
              "WHILE [[2]]; " +
              "MOV 0 [[3]]; " +
              "MOV 1 [[4]]; " +
              "SUB 1 [[0]] [[1]]; " +
              "ZERO [[2]]; " +
              "ENDWHILE; " +
              "WHILE [[3]]; " +
              "ZERO [[0]]; " +
              "ZERO [[1]]; " +
              "ZERO [[3]]; " +
              "ENDWHILE; " +
              "ENDWHILE; " +
              "WHILE [[1]]; " +
              "MOV 1 [[4]]; " +
              "ZERO [[1]]; " +
              "ENDWHILE; ";
			resultCell = "[[4]]";
		}	
		else if (pred == Predicate.LT) {
			earCommand += 
              "MOV 0 [[4]]; " +
			  "WHILE [[0]]; " +
              "MOV 0 [[4]]; " +
              "COPY [[1]] [[2]] [[5]]; " +
              "MOV 1 [[3]]; " +
              "WHILE [[2]]; " +
              "MOV 0 [[3]]; " +
              "MOV 1 [[4]]; " +
              "SUB 1 [[0]] [[1]]; " +
              "ZERO [[2]]; " +
              "ENDWHILE; " +
              "WHILE [[3]]; " +
              "ZERO [[0]]; " +
              "ZERO [[1]]; " +
              "ZERO [[3]]; " +
              "ZERO [[4]]; " +
              "ENDWHILE; " +
              "ENDWHILE; " +
              "WHILE [[1]]; " +
              "MOV 1 [[4]]; " +
              "ZERO [[1]]; " +
              "ENDWHILE; ";
			resultCell = "[[4]]";
		}
		else {
			resultCell = "";
			throw new InvalidOperationTokenException(
					                 "Unknown operation token " + pred.name());
		}					
		
		Variable targetVar = mSymbols.getNewInternalVariable();
		String targetVarName = targetVar.getEARReference(this);
				
		earCommand += "COPY " + resultCell + " " + targetVarName + ";";
		
		for (int i = 0; i < 6; i++) {
			String s = Integer.toString(i);
			earCommand = earCommand.replaceAll("\\[\\[" + s + "\\]\\]", 
					                           mWorkingMemory[i]);
		}
		
		mOutput += earCommand;
		
		return targetVar; // TODO;
	}
}
