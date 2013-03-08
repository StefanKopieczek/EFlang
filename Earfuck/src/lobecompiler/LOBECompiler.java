package lobecompiler;

import java.util.Stack;

import earcompiler.EARCompiler;
import earcompiler.EARException;

public class LOBECompiler {
		
	private LOBESymbolTable mSymbols;
	private EARCompiler earCompiler;
	private String mOutput;	
	private String[] mWorkingMemory;
	private Stack<Variable> mIfs;
	
	public LOBECompiler() {
		mSymbols = new LOBESymbolTable();
		initWorkingMemory(10);
		mOutput = "";
	}
	
	private void execute(LOBEInstruction instruction) 
			throws InvalidParameterException {
		if (instruction.mCommand == LOBECommand.PRINT) {
			if (instruction.mArguments.length != 1) {
				throw new InvalidParameterException("PRINT takes exactly one parameter.");
			}
			Value argval = instruction.mArguments[0].evaluate(this);
			mOutput += "OUT " + argval.getEARReference(this);			
		}
		else if (instruction.mCommand == LOBECommand.SET) {
			if (instruction.mArguments.length != 2) {
				throw new InvalidParameterException("SET takes exactly two parameters.");
			}
			if (!(instruction.mArguments[0] instanceof Variable)) {
				throw new InvalidParameterException("The first argument to SET must be a variable.");
			}
			Variable target = (Variable)instruction.mArguments[0];
			Value source = instruction.mArguments[1].evaluate(this);
			mOutput += "COPY " + source.getEARReference(this) + " " + mSymbols.get(target) + " " + mWorkingMemory[0];
		}
		else if (instruction.mCommand == LOBECommand.IF) {
			
		}
	}
	
	private void initWorkingMemory(int size) {
		mWorkingMemory = new String[size];
		for (int i = 0; i < size; i++) {
			mSymbols.put(new Variable("!w"+i), i);
			mWorkingMemory[i] = "!w"+i;			
		}			
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
		
		if (op == Operator.ADD) {
			opName = "ADD";
		}
		else if (op == Operator.MUL) {
			opName = "MUL";
		}
		else if (op == Operator.SUB) {
			opName = "SUB";
		}
		else {
			throw new InvalidOperationTokenException(
					                   "Unknown operation token " + op.name());
		}					
		
		Variable targetVar = mSymbols.getNewInternalVariable();
		String targetVarName = targetVar.getEARReference(this);
		
		if (op != Operator.MUL){ 
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
		
		try {
			mOutput += earCompiler.compile(earCommand);
		}
		catch(EARException e) {
			// Hmm this is sad but oh well.
		}
		
		return targetVar; // TODO;
	}
	
	public Variable evaluate(Predicate pred, Value val1, Value val2) {		
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
