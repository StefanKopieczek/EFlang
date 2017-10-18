package eflang.lobe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class LOBECompiler {

    private LOBESymbolTable mSymbols;
    public String mOutput;
    private Stack<Variable> mIfs;
    private Stack<Evaluable> mWhileConditions;
    private Stack<Variable> mWhileVars;
    private ArrayList<Integer> mCommandStartPositions;

    public LOBECompiler() {
        reset();
    }

    /**
     * Resets state to that of a new compiler.
     */
    private void reset() {
        mSymbols = new LOBESymbolTable();
        mOutput = "";
        mIfs = new Stack<Variable>();
        mWhileConditions = new Stack<Evaluable>();
        mWhileVars = new Stack<Variable>();
        mCommandStartPositions = new ArrayList<Integer>();
    }

    public ArrayList<Integer> getCommandStartPositions() {
        return mCommandStartPositions;
    }

    /**
     * Compiles the given LOBE code and returns the compiled EAR code as a string.
     * @param LOBECode Code to compile.
     * @return The Compiled EAR code.
     * @throws InvalidParameterException
     */
    public String compile(String LOBECode)
        throws LobeCompilationException
    {
        reset();
        LOBEParser parser = new LOBEParser();
        LOBEInstruction[] instructions = parser.parseAll(LOBECode);
        for (LOBEInstruction instruction : instructions) {
            execute(instruction);
        }
        return mOutput;
    }

    public void execute(LOBEInstruction instruction)
        throws LobeCompilationException
    {
        mCommandStartPositions.add(mOutput.split("\\n+").length);

        // 'Case' statements don't have their own scope, so generic names are initialised here.
        // This makes me sad. It's like global variables are a thing again.
        Value argval;
        Variable target;
        Variable ifVar;
        Evaluable whileCondition;
        Variable whileVar;
        Value whileValue;
        
        switch(instruction.mCommand) {

            // -- PRINT statement --
            // Outputs the value of the given variable / expression.
            case PRINT:

                if (instruction.mArguments.length != 1) {
                    throw new InvalidParameterException("PRINT takes exactly one parameter.");
                }

                // Reduce the expression in the argument to either a variable or a constant.
                argval = instruction.mArguments[0].evaluate(this);

                if (argval instanceof Variable) {
                    // If we're printing a variable, we only need to output the
                    // value of that variable - which is just an OUT statement.
                    target = (Variable)argval;
                }
                else
                {
                    // If we're printing the value of a constant, we have to put it
                    // into a cell first, as EAR's 'OUT' instruction only works on cells.
                    target = mSymbols.getNewInternalVariable(this);
                    mOutput += "MOV " + argval.getRef(this) + " " + target.getRef(this) + "\n";
                }

                mOutput += "OUT " + target.getRef(this) + "\n";
                break;

            // -- INPUT statement --
            // Takes a variable name, prompts the user for input, and stores it in that variable.
            case INPUT:
                if (instruction.mArguments.length != 1) {
                    throw new InvalidParameterException("INPUT takes exactly one parameter.");
                }
                if (!(instruction.mArguments[0] instanceof Variable)) {
                    throw new InvalidParameterException(
                            "The argument to INPUT must be a variable.");
                }

                target = (Variable)instruction.mArguments[0];
                mOutput += "IN " + target.getRef(this) + "\n";
                break;

            // -- SET statement --
            // Assigns to the variable in the first argument the value of the expression in the
            // second argument.
            case SET:
                if (instruction.mArguments.length != 2) {
                    throw new InvalidParameterException(
                            "SET takes exactly two parameters.");
                }
                if (!(instruction.mArguments[0] instanceof Variable)) {
                    throw new InvalidParameterException(
                            "The first argument to SET must be a variable.");
                }

                // Interpret the first argument as a variable, and reduce the
                // second to either a Variable or a Constant.
                target = (Variable) instruction.mArguments[0];
                argval = instruction.mArguments[1].evaluate(this);

                if (!mSymbols.containsKey(target)) {
                    mSymbols.addVariable(target, this, justVariables(argval));
                }

                // The format of EAR's 'IN' instruction is such that if the
                // second argument is a variable, it must be preceded by an '@'.
                String maybeAt = (argval instanceof Variable) ? "@" : "";

                Variable[] siblings;
                if (argval instanceof Variable)
                {
                    siblings = new Variable[] {(Variable)argval, target};
                }
                else
                {
                    siblings = new Variable[] {target};
                }
                mOutput += "COPY " + maybeAt + argval.getRef(this)
                        + " " + target.getRef(this)
                        + " " + mSymbols.getNewInternalVariable(this, siblings).getRef(this)
                        + "\n\n";

                break;

            // -- IF statement --
            // The code within the IF block (i.e. until the matching ENDIF)
            // is executed if and only if the conditional expression provided
            // as an argument is true.
            case IF:
                if (instruction.mArguments.length != 1) {
                    throw new InvalidParameterException(
                            "IF takes exactly one parameter.");
                }

                // Evaluate the conditional expression.
                // It returns a value, which will be nonzero iff the
                // condition is true.
                Value conditional = instruction.mArguments[0].evaluate(this);

                // We need to copy said value into a cell that is protected
                // throughout the course of the IF block - as we need to ZERO
                // it to ENDIF.
                ifVar = mSymbols.getNewInternalVariable(this);
                maybeAt = (conditional instanceof Variable) ? "@" : "";
                mOutput += "COPY " + maybeAt + conditional.getRef(this)
                        + " " + ifVar.getRef(this)
                        + " " + mSymbols.getNewInternalVariable(this, justVariables(ifVar)).getRef(this)
                        + "\n";
                mIfs.add(ifVar); // Remember for later ENDIF.
                mSymbols.lockVariable(ifVar); // Don't garbage collect yet!
                mOutput += "WHILE " + ifVar.getRef(this) + "\n\n";
                break;

            // -- ENDIF statement --
            // Signals the end of an IF block.
            case ENDIF:
                if (instruction.mArguments.length != 0) {
                    throw new InvalidParameterException(
                            "ENDIF takes no parameters.");
                }

                // ZERO the variable on which we were conditioning, so that the
                // underlying EAR 'WHILE' block terminates.
                ifVar = mIfs.pop();
                mOutput += "ZERO " + ifVar.getRef(this) + "\n";
                mOutput += "ENDWHILE\n\n";

                // Unlock the variable so that it can be garbage collected.
                mSymbols.unlockVariable(ifVar);
                break;

            // -- WHILE statement --
            // All the code in a WHILE block (i.e. until the matching ENDWHILE)
            // is evaluated only if the given conditional expression is true;
            // and is repeatedly evaluated until it happens that we reach the
            // end of the block and the conditional happens to be false.
            case WHILE:
                if (instruction.mArguments.length != 1) {
                    throw new InvalidParameterException(
                            "WHILE takes exactly one parameter.");
                }

                whileCondition = instruction.mArguments[0];
                whileValue = whileCondition.evaluate(this);
                if (whileValue instanceof Constant) {
                    // Our conditional comes out to a fixed constant.
                    // So we save it into a variable so we can loop on it.
                    whileVar = mSymbols.getNewInternalVariable(this);
                    mOutput += "MOV " + whileValue.getRef(this)
                            + " " + whileVar.getRef(this)
                            + "\n";
                }
                else {
                    whileVar = (Variable)whileValue;
                }

                // Don't garbage collect the loop variable!
                mSymbols.lockVariable(whileVar);

                // Remember the condition of the loop so we can re-evaluate it
                // when we come to repeat. 
                // Also remember the variable we're conditioning on, as we'll want
                // to re-evaluate the conditional to that cell.
                mWhileConditions.add(whileCondition);
                mWhileVars.add(whileVar);

                mOutput += "WHILE " + whileVar.getRef(this) + "\n\n";
                break;

            // -- ENDWHILE --
            // Signals the end of a WHILE block.
            case ENDWHILE:
                if (instruction.mArguments.length != 0) {
                    throw new InvalidParameterException(
                            "ENDWHILE takes no parameters.");
                }

                // Re-evaluate the conditional to the same variable as before.
                whileCondition = mWhileConditions.pop();
                whileVar = mWhileVars.pop();
                whileValue = whileCondition.evaluate(this, whileVar);

                // We can garbage collect the loop variable now.
                mSymbols.unlockVariable(whileVar);

                mOutput += "ENDWHILE\n\n";
                break;

            default:
                throw new InvalidOperationTokenException("Invalid command "
                        + instruction.mCommand);
        }

        mSymbols.clearInternalVars();
    }

    public String getRef(Constant c) {
        return Integer.toString(c.getValue());
    }

    public String getRef(Variable v) {
        return Integer.toString(getPointer(v));
    }

    public void registerVariableNearSiblings(Variable v, Variable... siblings) {
        if (mSymbols.containsKey(v)) {
            throw new VariableAlreadyRegisteredException(v);
        }

        ArrayList<Integer> siblingCells = new ArrayList<Integer>();
        for (Variable sibling : siblings) {
            siblingCells.add(mSymbols.get(sibling));
        }

        // Not super-efficient, but the list should be short.
        Integer[] sortedSiblings = siblingCells.toArray(new Integer[siblingCells.size()]);
        Arrays.sort(sortedSiblings);
        int minSiblingCell = sortedSiblings[0];
        int maxSiblingCell = sortedSiblings[sortedSiblings.length - 1];

        boolean minReached = false;
        boolean maxReached = false;
        int leftGuess = mSymbols.get(siblings[0]) - 1;
        int rightGuess = leftGuess + 2;
        int targetCell;
        while (true) {
            if (!minReached || maxReached) {
                if (mSymbols.isCellFree(leftGuess)) {
                    targetCell = leftGuess;
                    break;
                }
                leftGuess--;
                if (leftGuess <= minSiblingCell) {
                    minReached = true;
                }
            }
            if (!maxReached || minReached) {
                if (mSymbols.isCellFree(rightGuess)) {
                    targetCell = rightGuess;
                    break;
                }
                rightGuess++;
                if (rightGuess >= maxSiblingCell) {
                    maxReached = true;
                }
            }
        }

        mSymbols.addVariable(v, targetCell);
    }

    public int getPointer(Variable v) {
        if (!mSymbols.containsKey(v)) {
            // The variable isn't in our symbol table - so create it.
            mSymbols.addVariable(v);
        }
        return mSymbols.get(v);
    }

    public Variable backup(Variable v, Variable target) {
        Variable workingCell = mSymbols.getNewInternalVariable(this, v, target);
        mOutput += "COPY @" + v.getRef(this) + " "
                            + target.getRef(this) + " "
                            + workingCell.getRef(this)
                            + "\n";
        mSymbols.deleteVariable(workingCell);
        return target;
    }

    /**
     * Apply the operator 'op' to val1 and val2, and return the resulting
     * value.
     * @param op The Operator to use.
     * @param val1 The LHS argument.
     * @param val2 The RHS argument.
     * @param targetVar If we yield a Variable as our answer, use this variable
     *        to store it.
     * @param forceVariable Always return a Variable, even if we could use a
     *        literal constant.
     * @return
     */
    public Value evaluate(Operator op,
                          Value val1,
                          Value val2,
                          Variable targetVar,
                          boolean forceVariable)
        throws InvalidOperationTokenException {
        Value result;

        // We require values to be either variables or constants.
        assert(val1 instanceof Variable || val1 instanceof Constant);
        assert(val2 instanceof Variable || val2 instanceof Constant);

        // Temp working cells assigned during this evaluation which can be garbage
        // collected afterward.
        ArrayList<Variable> tempVars = new ArrayList<Variable>();

        switch (op) {
            case ADD:
                if (val2 instanceof Variable) {
                    // 2nd arg is destroyed by ADD, so use a temp copy.
                    if (targetVar == null) {
                        targetVar = mSymbols.getNewInternalVariable(this, justVariables(val2, val1));
                    }
                    val2 = backup((Variable)val2, targetVar);
                }
                if (val1 instanceof Variable) {
                    if (val2 instanceof Constant) {
                        // Can't use a constant as the second argument, so back 1st arg up
                        // and switch the order of the arguments.
                        if (targetVar == null) {
                            targetVar = mSymbols.getNewInternalVariable(this, justVariables(val1, val2));
                        }
                        Value temp = backup((Variable)val1, targetVar);
                        val1 = val2;
                        val2 = temp;
                    }
                    else {
                        // Back up the first argument.
                        val1 = backup((Variable)val1,
                                      mSymbols.getNewInternalVariable(this, justVariables(val1, val2, targetVar)));
                    }
                }
                if (val1 instanceof Variable && val2 instanceof Constant) {
                    // 2nd arg is a constant - this isn't allowed in EAR but since 1st arg is a
                    // variable we can swap them over.
                    Value temp = val1;
                    val1 = val2;
                    val2 = temp;
                }
                if (val1 instanceof Constant && val2 instanceof Constant) {
                    // Both arguments are constants - we just return their sum as a constant.
                    int num1 = ((Constant)val1).getValue();
                    int num2 = ((Constant)val2).getValue();
                    result = new Constant(num1 + num2);
                }
                else {
                    String maybeAt = (val1 instanceof Variable) ? "@" : "";
                    mOutput += "ADD " + maybeAt + val1.getRef(this) +
                                  " " + val2.getRef(this) +
                                  "\n";
                    result = val2;
                }
                break;

            case SUB:
                if (val1 instanceof Constant && val2 instanceof Constant) {
                    // Both arguments are constants; just calculate the result now and dump
                    // the result to the target cell.
                    int answer = ((Constant)val1).getValue() - ((Constant)val2).getValue();
                    result = new Constant(answer);

                    assert (targetVar == null);

                    break;
                }

                if (targetVar == null) {
                    targetVar = mSymbols.getNewInternalVariable(this, justVariables(val1, val2));
                }

                if (val1 instanceof Variable) {
                    // SUB stores its result in the memory location of the second argument.
                    // Copy val1 (which will be the 2nd argument of SUB) to the target cell.
                    backup((Variable)val1, targetVar);
                }
                else {
                    // SUB requires its second argument (our val1) to be a pointer,
                    // so dump val1 to memory as it's a constant.
                    mOutput += "MOV " + ((Constant)val1).getValue() +
                                  " " + targetVar.getRef(this) +
                                  "\n";
                }

                if (val2 instanceof Variable &&
                    !mSymbols.isInternalVariable((Variable)val2)) {
                    Variable workingCell = mSymbols.getNewInternalVariable(this, justVariables(val2, val1, targetVar));
                    tempVars.add(workingCell);
                    val2 = backup((Variable)val2, workingCell);
                }

                String maybeAt = (val2 instanceof Variable) ? "@" : "";

                mOutput += "SUB " + maybeAt + val2.getRef(this) +
                              " " + targetVar.getRef(this) +
                              "\n";
                result = targetVar;

                break;

            case MUL:
                if (val1 instanceof Variable) {
                    // 1st arg is destroyed by MUL, so use a temp copy.
                    val1 = backup((Variable)val1,
                                  mSymbols.getNewInternalVariable(this, justVariables(val1, val2, targetVar)));
                    tempVars.add((Variable)val1);
                }
                if (val2 instanceof Variable) {
                    // 2nd arg is destroyed by MUL, so use a temp copy.
                    val2 = backup((Variable)val2,
                                  mSymbols.getNewInternalVariable(this, justVariables(val1, val2, targetVar)));
                    tempVars.add((Variable)val2);
                }
                if (val1 instanceof Constant && val2 instanceof Constant) {
                    // Both arguments are constants - we just return the product as a constant.
                    int num1 = ((Constant)val1).getValue();
                    int num2 = ((Constant)val2).getValue();
                    result = new Constant(num1 * num2);
                }
                else {
                    if (targetVar == null) {
                        // We don't have an explicit target, so create an internal variable.
                        // Don't add it to the tempVars list as it needs to be returned to the caller.
                        targetVar = mSymbols.getNewInternalVariable(this, justVariables(val1, val2));
                    }

                    String maybeAt1 = (val1 instanceof Variable) ? "@" : "";
                    String maybeAt2 = (val2 instanceof Variable) ? "@" : "";
                    Variable workingCell = mSymbols.getNewInternalVariable(this, justVariables(val1, targetVar, val2));
                    tempVars.add(workingCell);
                    mOutput += "MUL " + maybeAt1 + val1.getRef(this) +
                                  " " + maybeAt2 + val2.getRef(this) +
                                   " " + targetVar.getRef(this) +
                                   " " + workingCell.getRef(this) +
                                   "\n";
                    result = targetVar;
                }
                break;
            case DIV:
                if (val1 instanceof Variable) {
                    // 1st arg is destroyed by DIV, so use a temp copy.
                    Variable workingCell = mSymbols.getNewInternalVariable(this, justVariables(val1, val2, targetVar));
                    val1 = backup((Variable)val1, workingCell);
                    tempVars.add(workingCell);
                }
                if (val2 instanceof Variable) {
                    // 2nd arg is destroyed by DIV, so use a temp copy.
                    Variable workingCell = mSymbols.getNewInternalVariable(this, justVariables(val1));
                    val2 = backup((Variable)val2, workingCell);
                    tempVars.add(workingCell);
                }
                if (val1 instanceof Constant && val2 instanceof Constant) {
                    // Both arguments are constants - we just return the product as a constant.
                    int num1 = ((Constant)val1).getValue();
                    int num2 = ((Constant)val2).getValue();
                    result = new Constant(num1 / num2);
                }
                else {
                    if (targetVar == null) {
                        targetVar = mSymbols.getNewInternalVariable(this, justVariables(val1, val2));
                    }

                    Variable[] workingCells = mSymbols.getNewInternalVariables(this, 6, justVariables(val1, val2, targetVar));
                    tempVars.addAll(Arrays.asList(workingCells));
                    String maybeAt1 = (val1 instanceof Variable) ? "@" : "";
                    String maybeAt2 = (val2 instanceof Variable) ? "@" : "";
                    mOutput += "DIV " + maybeAt1 + val1.getRef(this) +
                                  " " + maybeAt2 + val2.getRef(this) +
                                   " " + targetVar.getRef(this);
                    for (int ii = 5; ii >= 0; ii--) {
                        // Add working cells in reversed order, for efficiency.
                        mOutput += " " + workingCells[ii].getRef(this);
                    }
                    mOutput += "\n";
                    result = targetVar;
                }
                break;
            default:
                throw new InvalidOperationTokenException("Unknown operation token "
                        + op.name());
        }
        if (forceVariable && !(result instanceof Variable)) {
            if (targetVar == null) {
                targetVar = mSymbols.getNewInternalVariable(this);
            }
            storeValue(result, targetVar);
            result = targetVar;
        }

        mSymbols.deleteVariables(tempVars.toArray(new Variable[tempVars.size()]));
        return result;
    }

    public Variable evaluate(Predicate pred, Value val1, Value val2, Variable target)
        throws InvalidOperationTokenException {

        if (pred == Predicate.GEQ || pred == Predicate.GT) {
            Value tmp = val1;
            val1 = val2;
            val2 = tmp;
            pred = (pred == Predicate.GT) ? Predicate.LT : Predicate.LEQ;
        }

        String arg1Name = val1.getRef(this);
        String arg2Name = val2.getRef(this);
        String resultCell;

        String earCommand = "";

        String maybeAt1 = (val1 instanceof Variable) ? "@" : "";
        String maybeAt2 = (val2 instanceof Variable) ? "@" : "";
        earCommand += "COPY " + maybeAt1 + arg1Name + " [[0]] [[2]] \n";
        earCommand += "COPY " + maybeAt2 + arg2Name + " [[1]] [[2]] \n";

        int workingCellsNeeded = 6;
        switch (pred) {
            case EQ:
                earCommand += "MOV 1 [[4]]\n" +
                              "WHILE [[0]]\n" +
                              "MOV 0 [[4]]\n" +
                              "COPY @[[1]] [[2]] [[3]]\n" +
                              "MOV 1 [[5]]\n" +
                              "WHILE [[2]]\n" +
                              "MOV 0 [[5]]\n" +
                              "MOV 1 [[4]]\n" +
                              "SUB 1 [[0]] [[1]]\n" +
                              "ZERO [[2]]\n" +
                              "ENDWHILE\n" +
                              "WHILE [[5]]\n" +
                              "ZERO [[0]]\n" +
                              "MOV 1 [[1]]\n" +
                              "ZERO [[5]]\n" +
                              "ENDWHILE\n" +
                              "ENDWHILE\n" +
                              "WHILE [[1]]\n" +
                              "MOV 0 [[4]]\n" +
                              "ZERO [[1]]\n" +
                              "ENDWHILE\n";
                resultCell = "[[4]]";
                break;

            case NEQ:
                earCommand += "MOV 0 [[4]]\n" +
                              "WHILE [[0]]\n" +
                              "MOV 1 [[4]]\n" +
                              "COPY @[[1]] [[2]] [[3]]\n" +
                              "MOV 1 [[5]]\n" +
                              "WHILE [[2]]\n" +
                              "MOV 0 [[5]]\n" +
                              "MOV 0 [[4]]\n" +
                              "SUB 1 [[0]] [[1]]\n" +
                              "ZERO [[2]]\n" +
                              "ENDWHILE\n" +
                              "WHILE [[5]]\n" +
                              "ZERO [[0]]\n" +
                              "MOV 1 [[1]]\n" +
                              "ZERO [[5]]\n" +
                              "ENDWHILE\n" +
                              "ENDWHILE\n" +
                              "WHILE [[1]]\n" +
                              "MOV 1 [[4]]\n" +
                              "ZERO [[1]]\n" +
                              "ENDWHILE\n";
                resultCell = "[[4]]";
                break;

            case LEQ:
                earCommand += "MOV 1 [[4]]\n" +
                              "WHILE [[0]]\n" +
                              "MOV 0 [[4]]\n" +
                              "COPY @[[1]] [[2]] [[3]]\n" +
                              "MOV 1 [[5]]\n" +
                              "WHILE [[2]]\n" +
                              "MOV 0 [[5]]\n" +
                              "MOV 1 [[4]]\n" +
                              "SUB 1 [[0]] [[1]]\n" +
                              "ZERO [[2]]\n" +
                              "ENDWHILE\n" +
                              "WHILE [[5]]\n" +
                              "ZERO [[0]]\n" +
                              "ZERO [[1]]\n" +
                              "ZERO [[5]]\n" +
                              "ENDWHILE\n" +
                              "ENDWHILE\n" +
                              "WHILE [[1]]\n" +
                              "MOV 1 [[4]]\n" +
                              "ZERO [[1]]\n" +
                              "ENDWHILE\n";
                resultCell = "[[4]]";
                break;

            case LT:
                earCommand += "MOV 0 [[4]]\n" +
                              "WHILE [[0]]\n" +
                              "MOV 0 [[4]]\n" +
                              "COPY @[[1]] [[2]] [[3]]\n" +
                              "MOV 1 [[5]]\n" +
                              "WHILE [[2]]\n" +
                              "MOV 0 [[5]]\n" +
                              "MOV 1 [[4]]\n" +
                              "SUB 1 [[0]] [[1]]\n" +
                              "ZERO [[2]]\n" +
                              "ENDWHILE\n" +
                              "WHILE [[5]]\n" +
                              "ZERO [[0]]\n" +
                              "ZERO [[1]]\n" +
                              "ZERO [[5]]\n" +
                              "ZERO [[4]]\n" +
                              "ENDWHILE\n" +
                              "MOV 0 [[4]]\n" +
                              "ENDWHILE\n" +
                              "WHILE [[1]]\n" +
                              "MOV 1 [[4]]\n" +
                              "ZERO [[1]]\n" +
                              "ENDWHILE\n";
                resultCell = "[[4]]";
                break;

            default:
                resultCell = "";
                throw new InvalidOperationTokenException("Unknown operation token "
                        + pred.name());
        }

        String targetPointer = target.getRef(this);

        earCommand += "COPY @" + resultCell + " " + targetPointer + " [[5]]\n";

        Variable[] workingCells = mSymbols.getNewInternalVariables(this, workingCellsNeeded, justVariables(val1, val2, target));

        for (int i = 0; i < workingCellsNeeded; i++) {
            String s = Integer.toString(i);
            earCommand = earCommand.replaceAll("\\[\\[" + s + "\\]\\]",
                    workingCells[i].getRef(this));
        }

        for (Variable var : workingCells) {
            mSymbols.deleteVariable(var);
        }

        mOutput += earCommand;

        return target; // TODO;
    }

    public Variable getNewInternalVariable() {
        return mSymbols.getNewInternalVariable(this);
    }

    public void storeValue(Value val, Variable var) {
        String maybeAt = (val instanceof Variable) ? "@" : "";
        Variable workingCell = mSymbols.getNewInternalVariable(this, justVariables(val, var));
        mOutput += "COPY " + maybeAt + val.getRef(this)
                + " " + var.getRef(this)
                + " " + workingCell.getRef(this)
                + "\n";
        mSymbols.deleteVariable(workingCell);
    }

    public static Variable[] justVariables(Value... values)
    {
        ArrayList<Variable> variables = new ArrayList<Variable>();
        for (Value value : values) {
            if (value != null && value instanceof Variable) {
                variables.add((Variable)value);
            }
        }

        return variables.toArray(new Variable[variables.size()]);
    }
}
