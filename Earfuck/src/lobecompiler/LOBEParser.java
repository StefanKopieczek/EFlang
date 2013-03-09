package lobecompiler;

import java.util.ArrayList;
import java.util.Arrays;

public class LOBEParser {

	public LOBEInstruction parseInstruction(String instrString) {
		String[] tokens = instrString.split(" +");
		LOBECommand command = parseCommand(tokens[0]);
		Evaluable[] args = parseArgs(Arrays.copyOfRange(tokens, 1,
				tokens.length));
		LOBEInstruction result = new LOBEInstruction(command, args);
		return result;
	}
	
	public LOBEInstruction[] parseAll(String instructions) {
		ArrayList<LOBEInstruction> lobeInstructions = new ArrayList<LOBEInstruction>();
		String[] instructionStrings = instructions.split("\r?\n+");
		for (String commandString : instructionStrings) {
			lobeInstructions.add(parseInstruction(commandString));
		}
		return lobeInstructions.toArray(new LOBEInstruction[0]);
	}

	public LOBECommand parseCommand(String commandString) {
		LOBECommand result = null;
		for (LOBECommand cmd : LOBECommand.values()) {
			if (commandString.toUpperCase().equals(cmd.toString())) {
				result = cmd;
			}
		}
		if (result == null) {
			throw new InvalidOperationTokenException("Invalid command: '" + commandString +"'");
		}
		return result;
	}

	public Evaluable parseArg(String argString) {
		Evaluable result = null;
		if (argString.charAt(0) != '(') {		
			if (argString.matches("\\d+")) {
				result = new Constant(Integer.parseInt(argString));
				// System.out.println(Integer.parseInt(argString) + " - " + result);
			} 
			else if (argString.matches("[^\\d].*")) {
				result = new Variable(argString);
				//System.out.println("'"+argString+"'" + " - " +result);
			} 
			else {
				throw new InvalidOperationTokenException(
						"Invalid variable name " + argString);
			}
		} 
		else if (argString.charAt(argString.length() - 1) == ')') {
			argString = argString.substring(1, argString.length() - 1);
			// System.out.println(argString);
			int OpIdx = -1;
			int bracketCount = 0;
			for (int idx = 0; idx < argString.length(); idx++) {
				char c = argString.charAt(idx);
				if (c == '(') {
					bracketCount++;
					continue;
				}
				else if (c == ')') {
					bracketCount--;
					continue;
				}
				else {
					if (bracketCount == 0) {
						for (Operator op : Operator.values()) {							
							if (c == op.mSymbol) {
								String left = argString.substring(0, idx);
								String right = argString.substring(idx + 1, argString.length());
								// System.out.println("Valuetree(" + op + ","+ left+","+ right);
								result =  new ValueTree(op, parseArg(left), parseArg(right));
							}
						}
						boolean skipping = false;
						for (Predicate pred : Predicate.values()) {
							if (skipping) {
								skipping = false;
								continue;
							}
							String s = String.valueOf(c);							
							String s2 = (idx+1 != argString.length()) ? String.valueOf(argString.charAt(idx+1)) : null;
							
							if (pred.mSymbol.equals(s+s2)) {
								String left = argString.substring(0, idx);
								String right = argString.substring(idx + 2, argString.length());
								//System.out.println("Conditional(" + pred + ","+ left+","+ right);
								result = new Conditional(pred, parseArg(left), parseArg(right));
								skipping = true;
							}	
							else if (pred.mSymbol.equals(s)) {
								String left = argString.substring(0, idx);
								String right = argString.substring(idx + 1, argString.length());
								//System.out.println("Conditional(" + pred + ","+ left+","+ right);
								result = new Conditional(pred, parseArg(left), parseArg(right));
							}
						}
					}
				}			
			}
		}
		if (result == null) {
			throw new InvalidOperationTokenException("Invalid arg: '" + argString +"'");
		}
		return result;
	}

	public Evaluable[] parseArgs(String[] argStrings) {
		ArrayList<Evaluable> resultList = new ArrayList<Evaluable>();
		for (String argString : argStrings) {
			resultList.add(parseArg(argString));
		}
		return resultList.toArray(new Evaluable[0]);
	}

}
