package eflang.lobe;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LOBEParser {

	public LOBEInstruction parseInstruction(String instrString)
	    throws InvalidOperationTokenException {
		LOBEInstruction result;
		result = parseAssignment(instrString);
		if (result == null) {
			// The expression isn't an assignment, so try alternatives.
			int firstSpace = instrString.indexOf(" ");
			firstSpace = (firstSpace < 0) ? instrString.length() : firstSpace;
			String commandString = instrString.substring(0, firstSpace);
			LOBECommand command = parseCommand(commandString);
			Evaluable[] args;
			if (firstSpace < instrString.length()) {
				String argString;
				argString = instrString.substring(firstSpace+1, instrString.length());
				args = parseArgs(argString.split(","));
			}
			else {
				args = new Evaluable[0];
			}
			result = new LOBEInstruction(command, args);
		}
		return result;
	}

	public LOBEInstruction[] parseAll(String instructions)
	    throws InvalidOperationTokenException {
		ArrayList<LOBEInstruction> lobeInstructions = new ArrayList<LOBEInstruction>();
		String[] instructionStrings = instructions.split("\r?\n+");
		for (String commandString : instructionStrings) {
			lobeInstructions.add(parseInstruction(commandString));
		}
		return lobeInstructions.toArray(new LOBEInstruction[0]);
	}

	/**
	 * Determines whether the given instruction string is an assignment (of the form LHS = RHS).
	 * If it is, returns a LOBEInstruction to SET the LHS to the RHS.
	 * @param instString The code string to parse.
	 * @return
	 * @throws InvalidOperationTokenException
	 */
	public LOBEInstruction parseAssignment(String instString)
		throws InvalidOperationTokenException
	{
		Pattern equalityPattern = Pattern.compile("([^<>!=]+)=([^=]+)"); // Bit hacky - fix later. @@SMK@@
		Matcher equalityMatcher = equalityPattern.matcher(instString);
		LOBEInstruction result = null;
		if (equalityMatcher.find()) {
			String lhs = equalityMatcher.group(1);
			String rhs = equalityMatcher.group(2);
			Evaluable args[] = {parseArg(lhs), parseArg(rhs)};
			result = new LOBEInstruction(LOBECommand.SET, args);
		}
		return result;
	}

	public LOBECommand parseCommand(String commandString)
	    throws InvalidOperationTokenException {
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

	public Evaluable parseArg(String argString)
	    throws InvalidOperationTokenException {
		Evaluable result = null;
		argString = argString.trim();
		String topLevel = getTopLevel(argString);

		boolean matchFound = false;

		// First check to see if there is a predicate in the top level.
		for (Predicate p : Predicate.values()) {
			int idx = topLevel.indexOf(p.mSymbol);
			if (idx != -1) {
				// There is - let's return a Conditional based on it, evaluating the
				// LHS and RHS recursively.
				int predLength = p.name().length();
				Evaluable LHS = parseArg(argString.substring(0, idx));
				Evaluable RHS = parseArg(argString.substring(idx + predLength,
						                                     argString.length()));
				result = new Conditional(p, LHS, RHS);
				matchFound = true;
				break;
			}
		}

		// The top level doesn't contain a predicate - check if it contains an operator.
		if (!matchFound) {
			for (Operator op : Operator.values()) {
				int idx = topLevel.indexOf(op.mSymbol);
				if (idx != -1) {
					// There is - let's return a ValueTree based on it, evaluating the
					// LHS and RHS recursively.
					int opLength = 1;
					Evaluable LHS = parseArg(argString.substring(0, idx));
					Evaluable RHS = parseArg(argString.substring(idx + opLength,
							                                     argString.length()));
					result = new ValueTree(op, LHS, RHS);
					matchFound = true;
					break;
				}
			}
		}

		// Check to see if we're all just one big bracketed expression.
		int idxLeft = argString.indexOf('(');
		int idxRight = argString.lastIndexOf(')');
		if (!matchFound && idxLeft != -1 && idxRight != -1) {
			// We are - so just parse the bit inside the brackets.
			result = parseArg(argString.substring(idxLeft+1, idxRight));
			matchFound = true;
		}

		// Check if we're a constant.
		if (!matchFound && argString.matches("\\d+")) {
			result = new Constant(Integer.parseInt(argString));
			matchFound = true;
		}

		if (!matchFound) {
			// Assume we're a variable. We should probably be more careful here,
			// but good compilers don't write invalid code anyway so we don't
			// need to worry.d
			result = new Variable(argString);
		}

		return result;

	}

	public String getTopLevel(String expression) {
		String result = "";
		int bracketDepth = 0;
		for (char c : expression.toCharArray()) {
			if (c == '(') {
				bracketDepth += 1;
			}
			else if (c == ')') {
				bracketDepth -= 1;
			}
			if (bracketDepth == 0) {
				result += c;
			}
			else {
				result += " ";
			}
		}
		return result;
	}
//
//	public Evaluable parseArgOld(String argString) {
//		Evaluable result = null;
//		if (argString.charAt(0) != '(') {
//			if (argString.matches("\\d+")) {
//				result = new Constant(Integer.parseInt(argString));
//				// System.out.println(Integer.parseInt(argString) + " - " + result);
//			}
//			else if (argString.matches("[^\\d].*")) {
//				result = new Variable(argString);
//				//System.out.println("'"+argString+"'" + " - " +result);
//			}
//			else {
//				throw new InvalidOperationTokenException(
//						"Invalid variable name " + argString);
//			}
//		}
//		else if (argString.charAt(argString.length() - 1) == ')') {
//			argString = argString.substring(1, argString.length() - 1);
//			// System.out.println(argString);
//			int OpIdx = -1;
//			int bracketCount = 0;
//			for (int idx = 0; idx < argString.length(); idx++) {
//				char c = argString.charAt(idx);
//				if (c == '(') {
//					bracketCount++;
//					continue;
//				}
//				else if (c == ')') {
//					bracketCount--;
//					continue;
//				}
//				else {
//					if (bracketCount == 0) {
//						for (Operator op : Operator.values()) {
//							if (c == op.mSymbol) {
//								String left = argString.substring(0, idx);
//								String right = argString.substring(idx + 1, argString.length());
//								// System.out.println("Valuetree(" + op + ","+ left+","+ right);
//								result =  new ValueTree(op, parseArg(left), parseArg(right));
//							}
//						}
//						boolean skipping = false;
//						for (Predicate pred : Predicate.values()) {
//							if (skipping) {
//								skipping = false;
//								continue;
//							}
//							String s = String.valueOf(c);
//							String s2 = (idx+1 != argString.length()) ? String.valueOf(argString.charAt(idx+1)) : null;
//
//							if (pred.mSymbol.equals(s+s2)) {
//								String left = argString.substring(0, idx);
//								String right = argString.substring(idx + 2, argString.length());
//								//System.out.println("Conditional(" + pred + ","+ left+","+ right);
//								result = new Conditional(pred, parseArg(left), parseArg(right));
//								skipping = true;
//							}
//							else if (pred.mSymbol.equals(s)) {
//								String left = argString.substring(0, idx);
//								String right = argString.substring(idx + 1, argString.length());
//								//System.out.println("Conditional(" + pred + ","+ left+","+ right);
//								result = new Conditional(pred, parseArg(left), parseArg(right));
//							}
//						}
//					}
//				}
//			}
//		}
//		if (result == null) {
//			throw new InvalidOperationTokenException("Invalid arg: '" + argString +"'");
//		}
//		return result;
//	}

	public Evaluable[] parseArgs(String[] argStrings)
	    throws InvalidOperationTokenException {
		ArrayList<Evaluable> resultList = new ArrayList<Evaluable>();
		for (String argString : argStrings) {
			resultList.add(parseArg(argString));
		}
		return resultList.toArray(new Evaluable[0]);
	}

}
