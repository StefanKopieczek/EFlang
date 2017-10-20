package eflang.ear;

import eflang.ear.composer.Composer;
import eflang.ear.composer.OnlyRunsComposer;
import eflang.ear.operation.Goto;
import eflang.ear.operation.Operation;
import eflang.ear.operation.Zero;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Compiler for the EAR language. <br/>
 * Takes in EAR code and compiles to raw EF.
 * @author Ryan Norris
 *
 */
public class EARCompiler {
    private HashMap<String,EARInstruction> instructionSet;

    private Composer composer;

    private int p; //Cell pointer
    private String currentNote;
    private int optimism;
    private Stack<Integer> branchLocStack;
    private Stack<String> branchNoteStack;
    private Stack<Integer> branchOptimismStack;

    /**
     * This stores the start position in the compiled EF code for each
     * EAR command.
     * Index = EAR command index
     * Value = first EF command index in the given EAR command
     */
    private ArrayList<Integer> lineStartPositions;

    public EARCompiler() {
        this(new OnlyRunsComposer(Scales.CMajor));
    }

    public EARCompiler(Composer composer) {
        this.composer = composer;
        resetState();
    }

    private void resetState() {
        p=0;
        currentNote = composer.getStartingNode();
        optimism = 0;
        instructionSet = getInstructionSet();
        branchLocStack = new Stack<>();
        branchNoteStack = new Stack<>();
        branchOptimismStack = new Stack<>();
        lineStartPositions = new ArrayList<>();
    }

    private HashMap<String,EARInstruction> getInstructionSet() {
        HashMap<String,EARInstruction> instructions = new HashMap<>();
        instructions.put("GOTO", GOTO);
        instructions.put("IN", IN);
        instructions.put("OUT", OUT);
        instructions.put("ADD", ADD);
        instructions.put("SUB", SUB);
        instructions.put("MUL", MUL);
        instructions.put("DIV", DIV);
        instructions.put("WHILE", WHILE);
        instructions.put("ENDWHILE", ENDWHILE);
        instructions.put("ZERO", ZERO);
        instructions.put("COPY",COPY);
        instructions.put("MOV",MOV);

        return instructions;
    }

    /**
     * Compiles provided EARCode into an EF program
     * @param EARCode full program code.
     * @return String containing EF program
     */
    public String compile(String EARCode) throws EARException {
        resetState();
        StringBuilder output = new StringBuilder();

        //Discard comments (OLD COMMENT STYLE = "\\([^\\)]*\\)")
        EARCode = EARCode.replaceAll("//.*\\n","\n");

        //Split into individual instructions
        String[] instructions = EARCode.split("(\\r?\\n)+");

        // Keep track of how many EF notes we've output.
        int numNotes = 0;

        output.append(currentNote);
        output.append(" ");

        for (int i=0; i<instructions.length; i++) {
            String compiledInstruction = "";
            String instruction = instructions[i].replaceAll(" +", " ");
            instruction = instruction.replaceAll("^ +", "");
            if (!instruction.equals("")) {
                String[] parsedInstruction = instruction.split(" ");

                String[] args = Arrays.copyOfRange(parsedInstruction, 1,
                                                parsedInstruction.length);
                String opcode = parsedInstruction[0];

                EARInstruction command = instructionSet.get(opcode);

                //Check opcode actually generated a command
                if (command==null) {
                    String message = "Invalid opcode at instruction "+i+":";
                    if (i>0) {
                        message += "\n"+(i-1)+": "+instructions[i-1];
                    }
                    message += "\n"+i+": "+instruction;
                    if (i<instructions.length-1) {
                        message += "\n"+(i+1)+": "+instructions[i+1];
                    }
                    throw new EARInvalidOpcodeException(message);
                }

                //Check validity of command and throw helpful error message
                if (!command.checkArgs(instruction)) {
                    String message = "Invalid instruction signature at instruction "+i+":";
                    if (i>0) {
                        message += "\n"+(i-1)+": "+instructions[i-1];
                    }
                    message += "\n"+i+": "+instruction;
                    if (i<instructions.length-1) {
                        message += "\n"+(i+1)+": "+instructions[i+1];
                    }
                    throw new EARInvalidSignatureException(message);
                }
                compiledInstruction = command.compile(args);
            }

            //calculate how many commands into the EF code we are
            //Add it to the array of start positions
            lineStartPositions.add(numNotes);

            numNotes += compiledInstruction.split(" ").length;

            output.append(compiledInstruction);
        }
        return output.toString();
    }

    public ArrayList<Integer> getCommandStartPositions() {
        return lineStartPositions;
    }

    //Some convenience methods, not accessible to the EAR programmer directly
    /**
     * Safely moves the pointer one to the left.
     * @return The EF code to make that happen.
     */
    private String moveLeft() {
        String output = "";

        //Decrement pointer
        p--;
        //Set optimisim
        optimism = -1;

        if (currentNote.equals(composer.bottomNote())) {
            // Oops, we were already at the bottom, so jump up at least 2 and then move left to compensate.
            // Do it carefully by stepping, using lowerNote could fail if we hit the bottom.
            currentNote = composer.higherNote(composer.nextNote(currentNote));
            output += currentNote + " ";
            currentNote = composer.prevNote(currentNote);
            output += currentNote + " ";
        }

        currentNote = composer.lowerNote(currentNote);
        output += currentNote + " ";
        return output;
    }

    /**
     * Safely moves the pointer one to the right.
     * @return The EF code to make that happen.
     */
    private String moveRight() {
        String output = "";

        //Increment pointer
        p++;
        //Set optimisim
        optimism = 1;

        if (currentNote.equals(composer.topNote())) {
            // Oops, we were already at the top, so jump down to the bottom and then move right to compensate.
            // Do it carefully by stepping, using higherNote could fail if we hit the bottom.
            currentNote = composer.lowerNote(composer.prevNote(currentNote));
            output += currentNote + " ";
            currentNote = composer.nextNote(currentNote);
            output += currentNote + " ";
        }

        currentNote = composer.higherNote(currentNote);
        output += currentNote + " ";
        return output;
    }

    private String goTo(int cell) {
        StringBuilder output = new StringBuilder();
        while (p<cell) {
            output.append(moveRight());
        }
        while (p>cell) {
            output.append(moveLeft());
        }
        return output.toString();
    }

    private String ensureHappy() {
        return setOptimism(1);
    }

    private String ensureSad() {
        return setOptimism(-1);
    }

    /**
     * Set optimism without changing pointer location.
     * Note may change.
     */
    private String setOptimism(int targetOptimism) {
        String output = "";

        if (optimism < targetOptimism) {
            // Need to get happy.
            output += moveLeft() + moveRight() + " ";
        }

        if (optimism > targetOptimism) {
            // Need to get sad.
            output += moveRight() + moveLeft() + " ";
        }

        return output;
    }

    /**
     * Safely adds one to current cell
     * @return The EF code to make it happen
     */
    private String increment() {
        String output = "";
        output += ensureHappy();
        output += currentNote + " ";
        return output;
    }

    /**
     * Safely adds one to current cell
     * @return The EF code to make it happen
     */
    private String decrement() {
        String output = "";
        output += ensureSad();
        output += currentNote + " ";
        return output;
    }

    /**
     * Changes current note to specified target note
     * without changing the pointer/optimism
     * @param target note to change to
     * @return compiled EF code
     */
    private String changeNoteTo(String target) {
        String output = "";
        int targetOptimism = optimism;

        if (currentNote.equals(target)) {
            return output;
        }
        //Move note away from ends
        if (currentNote.equals(composer.bottomNote())) {
            currentNote = composer.nextNote(composer.nextNote(currentNote));
            output += currentNote + " ";
            currentNote = composer.prevNote(currentNote);
            output += currentNote + " ";
        }
        if (currentNote.equals(composer.topNote())) {
            currentNote = composer.prevNote(composer.prevNote(currentNote));
            output += currentNote + " ";
            currentNote = composer.nextNote(currentNote);
            output += currentNote + " ";
        }

        int noteCompare = composer.compareNotes(currentNote, target);
        if (noteCompare < 0) {
            output += composer.prevNote(currentNote) + " ";
            output += target + " ";
            currentNote = target;
        } else if (noteCompare > 0) {
            output += composer.nextNote(currentNote) + " ";
            output += target + " ";
            currentNote = target;
        }

        // Manually restore optimism if necessary.
        if (optimism < targetOptimism) {
            // Need to get happy.
            output += moveLeft() + target + " ";
        } else if (optimism > targetOptimism) {
            // Need to get sad.
            output += moveRight() + target + " ";
        }
        optimism = targetOptimism;
        currentNote = target;

        return output;
    }

    public class EARInstruction{
        private String signature;

        EARInstruction(String sig) {
            signature = sig;
        }

        /**
         * Checks if the given arg string is of the correct signature.
         * @param args arg string to check
         * @return True/False
         */
        boolean checkArgs(String args) {
            return args.matches(signature);
        }

        public String compile(String[] args){
            return "";
        }
    }

    // Compile from intermediate instructions.
    private String compileInstruction(Instruction instruction) {
        StringBuilder output = new StringBuilder();
        switch (instruction.getType()) {
            case GOTO:
                output.append(goTo(instruction.getValue()));
                break;

            case INCREMENT:
                output.append(increment());
                break;

            case DECREMENT:
                output.append(decrement());
                break;

            case START_LOOP:
                branchLocStack.push(p);
                branchNoteStack.push(currentNote);
                branchOptimismStack.push(optimism);
                output.append("( ");
                break;

            case END_LOOP:
                // Ensure same pointer location, optimism and note as start of loop.
                // The order here is important due to the guarantees each of these operations provides.
                output.append(goTo(branchLocStack.pop()));
                output.append(setOptimism(branchOptimismStack.pop()));
                output.append(changeNoteTo(branchNoteStack.pop()));
                output.append(") ");
                break;

            case ENSURE_HAPPY:
                output.append(ensureHappy());
                break;

            case ENSURE_SAD:
                output.append(ensureSad());
                break;

            case INPUT:
                output.append(ensureSad());
                output.append("r ");
                break;

            case OUTPUT:
                output.append(ensureHappy());
                output.append("r ");
                break;
        }
        return output.toString();
    }

    private String compileOperation(Operation op, List<Argument> args) {
        return String.join(" ",
                op.compile(args).stream()
                        .map(instruction -> compileInstruction(instruction))
                        .collect(Collectors.toList()));
    }

    private static List<Argument> parseArgs(String[] args) {
        return Arrays.asList(args).stream()
                .map(EARCompiler::parseArg)
                .collect(Collectors.toList());
    }

    private static Argument parseArg(String arg) {
        if (arg.startsWith("@")) {
            return Argument.cell(Integer.parseInt(arg.substring(1)));
        } else {
            return Argument.constant(Integer.parseInt(arg));
        }
    }

    //Defines all the instructions

    /**
     * Moves the pointer to specified cell.
     * e.g.
     * GOTO 5;
     */
    private EARInstruction GOTO = new EARInstruction("GOTO\\s+-?\\d+\\s*") {
        public String compile(String[] args) {
            return compileOperation(new Goto(), parseArgs(args));
        }
    };

    /**
     * Resets target cell to zero
     * e.g.
     * ZERO 5;
     */
    private EARInstruction ZERO = new EARInstruction("ZERO\\s+-?\\d+\\s*") {
        public String compile(String[] args) {
            return compileOperation(new Zero(), parseArgs(args));
        }
    };

    /**
     * Takes input to target cell
     * e.g.
     * IN 5;
     */
    private EARInstruction IN = new EARInstruction("IN\\s+-?\\d+\\s*") {
        public String compile(String[] args) {
            String output = "";
            if (args.length!=0){
                //goto cell
                output += GOTO.compile(args);
            }

            //ensure pessimism
            output += ensureSad();

            //take input
            output += "r ";

            return output;
        }
    };

    /**
     * Outputs target cell.
     * e.g.
     * OUT 5;
     */
    private EARInstruction OUT = new EARInstruction("OUT\\s+-?\\d+\\s*") {
        public String compile(String[] args) {
            String output = "";
            if (args.length!=0){
                //goto cell
                output += GOTO.compile(args);
            }

            //ensure optimism
            output += ensureHappy();

            //give output
            output += "r ";

            return output;
        }
    };

    /**
     * Begins a loop conditional on the target cell.
     * Should be matched with an WHILE
     * e.g.
     * WHILE 5;
     */
    private EARInstruction WHILE = new EARInstruction("WHILE\\s+-?\\d+\\s*") {
        public String compile(String[] args) {
            String output = "";
            if (args.length!=0){
                //goto cell
                output += GOTO.compile(args);
            }

            output += "( ";

            //Store where we were when we came in
            branchLocStack.push(p);
            branchNoteStack.push(currentNote);
            branchOptimismStack.push(optimism);

            return output;
        }
    };

    /**
     * Returns to start of loop if conditioned cell is non-0
     * Conditioned cell chosen by previous maching IF.
     * e.g.
     * REPIF;
     */
    private EARInstruction ENDWHILE = new EARInstruction("ENDWHILE") {
        public String compile(String[] args) {
            String output = "";
            int branchExitPoint = branchLocStack.pop();

            //return to branch exit point
            output += GOTO.compile(new String[]{String.valueOf(branchExitPoint)});

            //ensure optimism same as start of loop
            int branchEntryOptimism = branchOptimismStack.pop();
            output += setOptimism(branchEntryOptimism);

            //ensure on same note as start of loop
            //(to ensure same behaviour in each loop)
            String branchEntryNote = branchNoteStack.pop();
            output += changeNoteTo(branchEntryNote);

            //exit branch
            output += ") ";

            return output;
        }
    };

    /**
     * Adds the value of the first argument (use @ for a pointer)
     * to the cells given by the remaining arguments (as many as you like)
     * THE SUMMAND IS DESTROYED (zeroed)
     * e.g.
     * ADD @5 2 3 4;
     * Adds the value in cell 5 to cells 2, 3 and 4.
     */
    private EARInstruction ADD = new EARInstruction(
            "ADD\\s+(@|@-)?\\d+\\s+(-?\\d+\\s+)*-?\\d+\\s*") {
        public String compile(String[] args) {
            StringBuilder output = new StringBuilder();
            int amount;

            //Parse & sort list of target cells
            ArrayList<Integer> targets = new ArrayList<>();
            for (String s : Arrays.copyOfRange(args, 1, args.length)) {
                targets.add(Integer.parseInt(s));
            }
            Collections.sort(targets);

            //If given pointer
            if (args[0].charAt(0)=='@') { //If pointer
                //Goto summand cell
                output.append(GOTO.compile(new String[]{args[0].substring(1)}));
                //Ensure pessimism
                if (optimism!=-1) {
                    output.append(moveRight());
                    output.append(moveLeft());
                }
                //Until cell is 0
                output.append(WHILE.compile(new String[]{args[0].substring(1)}));
                output.append(decrement());

                //for each target cell
                for (int index : targets) {
                    //Goto target cell
                    output.append(GOTO.compile(new String[]{String.valueOf(index)}));
                    output.append(increment());
                }
                //Return to summand cell
                output.append(GOTO.compile(new String[]{args[0].substring(1)}));
                //Ensure pessimism
                if (optimism!=-1) {
                    output.append(moveRight());
                    output.append(moveLeft());
                }
                //end loop
                output.append(ENDWHILE.compile(new String[]{}));
            }
            else { //If given absolute
                amount = Integer.parseInt(args[0]);
                //for each target cell
                for (int index : targets) {
                    //Goto target cell
                    output.append(GOTO.compile(new String[]{String.valueOf(index)}));
                    for (int i=0;i<amount;i++) {
                        output.append(increment());
                    }

                }
            }
            return output.toString();
        }
    };

    /**
     * Subtracts the value of the first argument (use @ for a pointer)
     * from the cells given by the remaining arguments (as many as you like)
     * THE SUBTRACTAND IS DESTROYED (zeroed)
     * e.g.
     * SUB @5 2 3 4;
     * Subtracts the value in cell 5 from cells 2, 3 and 4.
     */
    private EARInstruction SUB = new EARInstruction(
            "SUB\\s+(@|@-)?\\d+\\s+(-?\\d+\\s+)*-?\\d+\\s*") {
        public String compile(String[] args) {
            StringBuilder output = new StringBuilder();
            int amount;

            //Parse & sort list of target cells
            ArrayList<Integer> targets = new ArrayList<>();
            for (String s : Arrays.copyOfRange(args, 1, args.length)) {
                targets.add(Integer.parseInt(s));
            }
            Collections.sort(targets);

            //If given pointer
            if (args[0].charAt(0)=='@') { //If pointer
                //Goto summand cell
                output.append(GOTO.compile(new String[]{args[0].substring(1)}));
                //Ensure pessimism
                if (optimism!=-1) {
                    output.append(moveRight());
                    output.append(moveLeft());
                }
                //Until cell is 0
                output.append(WHILE.compile(new String[]{args[0].substring(1)}));
                output.append(decrement());

                //for each target cell
                for (int index : targets) {
                    //Goto target cell
                    output.append(GOTO.compile(new String[]{String.valueOf(index)}));
                    output.append(decrement());
                }
                //Return to summand cell
                output.append(GOTO.compile(new String[]{args[0].substring(1)}));
                //Ensure pessimism
                if (optimism!=-1) {
                    output.append(moveRight());
                    output.append(moveLeft());
                }

                //end loop
                output.append(ENDWHILE.compile(new String[]{}));
            }
            else { //If given absolute
                amount = Integer.parseInt(args[0]);
                //for each target cell
                for (int index : targets) {
                    //Goto target cell
                    output.append(GOTO.compile(new String[]{String.valueOf(index)}));
                    for (int i=0;i<amount;i++) {
                        output.append(decrement());
                    }

                }
            }
            return output.toString();
        }
    };

    /**
     * Multiplies two values into the given cell.
     * The final argument specifies a working cell
     * THE TWO MULTIPLICANDS ARE DESTROYED (zeroed).
     * THE WORKING CELL IS NOT ZEROED
     * e.g.
     * MUL @5 @3 1 0
     * Multiplies cell 5 with cell 3, stores the answer in cell 1,
     * and uses cell 0 for working.
     */
    private EARInstruction MUL = new EARInstruction(
            "MUL\\s+((@|@-)?\\d+\\s*){2}-?\\d+\\s+-?\\d+\\s*") {
        public String compile(String[] args) {
            String tempA,tempB;
            StringBuilder output = new StringBuilder();
            String targetCell = args[2];
            String workingCell = args[3];

           //Zero the target cell
            output.append(ZERO.compile(new String[]{targetCell}));

            if (args[0].charAt(0)=='@') {
                if (args[1].charAt(0)=='@') {
                    //BOTH REFERENCES - HARD CASE
                    //Clear working cell
                    output.append(ZERO.compile(new String[]{workingCell}));
                    //Get cell indices
                    tempA = args[0].substring(1);
                    tempB = args[1].substring(1);

                    //loop on tempA
                    output.append(WHILE.compile(new String[]{tempA}));
                    //subtract one from tempA
                    output.append(decrement());

                    //move tempB back where it was
                    //Note, this doesn't really make sense in the first
                    //iteration, however it won't do anything the first time round
                    //(except move to the workingCell)
                    //Having it at the start of the loop means it won't move
                    //one of the arguments back into it's original cell after we're done
                    //this is always a time-improvement.
                    //This also makes behaviour consistent, in that
                    //both argument cells are destroyed after the algorithm is done.
                    output.append(ADD.compile(new String[]{"@"+workingCell,tempB}));

                    //add tempB to target & working space
                    output.append(ADD.compile(new String[] {"@"+tempB,targetCell,workingCell}));

                    //end loop
                    output.append(ENDWHILE.compile(new String[]{}));

                    //DONE!
                    return output.toString();
                }
                else {
                    //tempA = cell reference
                    //tempB = absolute value
                    tempA = args[0];
                    tempB = args[1];
                }
            }
            else {
                if (args[1].charAt(0)=='@') {
                    //tempA = cell reference
                    //tempB = absolute value
                    tempA = args[1];
                    tempB = args[0];
                }
                else {
                    //If both absolute, just do it and add
                    int a = Integer.parseInt(args[0]);
                    int b = Integer.parseInt(args[1]);
                    output.append(ADD.compile(new String[] {String.valueOf(a*b),targetCell}));
                    return output.toString();
                }
            }

            //Now we're definitely in the 1 reference, 1 absolute case
            //get value of absolute
            int absoluteValue = Integer.parseInt(tempB);
            //Clear working cell
            output.append(ZERO.compile(new String[]{workingCell}));
            //just add that many times
            for (int i=0; i<absoluteValue; i++) {
                output.append(ADD.compile(new String[]{tempA,targetCell,workingCell}));
                output.append(ADD.compile(new String[]{"@"+workingCell,tempA.substring(1)}));
            }
            return output.toString();
        }
    };

    /**
     * Divides the first argument by the second, and puts the result
     * in the cell specified by the third argument.
     * The final arguments specify working cells (6 in total)
     * THE TWO DIVISANDS ARE DESTROYED (zeroed).
     * By an awesome coincidence, the numerator will be left as the remainder.
     * THE WORKING CELLS ARE NOT ZEROED
     * e.g.
     * DIV @5 @3 1 0 2 4 6 7
     * Divides cell 5 by cell 3, stores the answer in cell 1,
     * and uses cells 0, 2, 4, 6 and 7 for working.
     */
    private EARInstruction DIV = new EARInstruction(
            "DIV\\s+((@|@-)?\\d+\\s*){2}(-?\\d+\\s+){6}-?\\d+") {
        public String compile(String[] args) {
            StringBuilder output = new StringBuilder();
            String numerator = args[0];
            String denominator = args[1];
            String targetCell = args[2];
            //This cell will contain a flag for if the division fails
            String flag = args[3];
            String workingCell1 = args[4];
            String workingCell2 = args[5];
            String workingCell3 = args[6];
            String workingCell4 = args[7];
            String workingCell5 = args[8];
            boolean numeratorIsReference = (numerator.charAt(0) == '@');
            boolean denominatorIsReference = (denominator.charAt(0) == '@');

            //Zero the target cell
            output.append(ZERO.compile(new String[]{targetCell}));

            if (!numeratorIsReference && !denominatorIsReference) {
                //Both absolute, just do the division at compile time.
                int a = Integer.parseInt(numerator);
                int b = Integer.parseInt(denominator);
                output.append(MOV.compile(new String[]{Integer.toString(a/b), targetCell}));
            }
            else {
                //We now know at least one arg isn't absolute.
                //If one IS absolute, put it in the working cell.
                if (!numeratorIsReference) {
                    output.append(MOV.compile(new String[]{numerator, workingCell1}));
                    numerator = "@" + workingCell1;
                }
                else if (!denominatorIsReference) {
                    output.append(MOV.compile(new String[]{denominator, workingCell1}));
                    denominator = "@" + workingCell1;
                }

                //Strip off @s
                numerator = numerator.substring(1, numerator.length());
                denominator = denominator.substring(1, denominator.length());

                //Now we have 2 values in cells. We should divide them.
                //Note: we may no longer use workingCell1, as it may contain
                //one of the numbers we're calculating with.

                //Zero the working cells we'll use for the division
                output.append(ZERO.compile(new String[]{workingCell2}));
                output.append(ZERO.compile(new String[]{workingCell3}));
                output.append(ZERO.compile(new String[]{workingCell4}));
                output.append(ZERO.compile(new String[]{workingCell5}));

                //Set the flag to 1
                output.append(MOV.compile(new String[]{"1",flag}));

                //Copy the denominator to working cell 2
                output.append(COPY.compile(new String[]{"@" + denominator, workingCell2, workingCell4}));

                //While numerator not 0
                output.append(WHILE.compile(new String[]{numerator}));

                //Use workingCell3 to indicate if one of the numbers hit 0, so we
                //don't try to subtract from it.
                output.append(MOV.compile(new String[]{"1", workingCell3}));

                output.append(WHILE.compile(new String[]{workingCell3}));

                //Subtract one from each
                output.append(SUB.compile(new String[]{"1",numerator}));
                output.append(SUB.compile(new String[]{"1",denominator}));

                //Work out if either hit 0
                //Proc for this is:
                //Copy into WC4, use WC5 as flag.

                //If numerator is 0
                output.append(COPY.compile(new String[]{"@" + numerator, workingCell4, workingCell5}));
                output.append(MOV.compile(new String[]{"1", workingCell5}));
                output.append(WHILE.compile(new String[]{workingCell4}));
                output.append(ZERO.compile(new String[]{workingCell5}));
                output.append(ZERO.compile(new String[]{workingCell4}));
                output.append(ENDWHILE.compile(new String[]{}));
                output.append(WHILE.compile(new String[]{workingCell5}));
                //Set flag
                output.append(ZERO.compile(new String[]{workingCell3}));
                output.append(ZERO.compile(new String[]{workingCell5}));
                output.append(ENDWHILE.compile(new String[]{}));

                //If denominator is 0
                output.append(COPY.compile(new String[]{"@" + denominator, workingCell4, workingCell5}));
                output.append(MOV.compile(new String[]{"1", workingCell5}));
                output.append(WHILE.compile(new String[]{workingCell4}));
                output.append(ZERO.compile(new String[]{workingCell5}));
                output.append(ZERO.compile(new String[]{workingCell4}));
                output.append(ENDWHILE.compile(new String[]{}));
                output.append(WHILE.compile(new String[]{workingCell5}));
                //Set flag, increment counter and refill denominator
                output.append(ZERO.compile(new String[]{workingCell3}));
                output.append(ZERO.compile(new String[]{workingCell5}));
                output.append(ADD.compile(new String[]{"1", targetCell}));
                output.append(COPY.compile(new String[]{"@" + workingCell2, denominator, workingCell4}));
                output.append(ENDWHILE.compile(new String[]{}));

                output.append(ENDWHILE.compile(new String[]{}));

                output.append(ENDWHILE.compile(new String[]{}));
            }

            return output.toString();
        }
    };


    /**
     * Copies one cell into all the given targets.
     * Uses the final argument as a working cell.
     * Note: Working cell is not used if value is absolute.
     * DOES NOT DESTROY THE ORIGINAL
     * e.g.
     * COPY @2 3 4 5;
     * Copies cell 2 into cells 3 & 4, using cell 5 as working space.
     */
    private EARInstruction COPY = new EARInstruction(
            "COPY\\s+(@|@-)?\\d+\\s+(-?\\d+\\s+)+-?\\d+\\s*") {
        public String compile(String[] args) {
            StringBuilder output = new StringBuilder();

            //If copying an absolute, use MOV, it's faster
            if (args[0].charAt(0)!='@') {
                String[] movArgs = Arrays.copyOfRange(args, 0, args.length-1);
                output.append(MOV.compile(movArgs));
                return output.toString();
            }

            //Zero the working cell
            output.append(ZERO.compile(new String[]{args[args.length-1]}));
            //Zero all target cells
            for (int i=1; i<args.length-1; i++) {
                output.append(ZERO.compile(new String[]{args[i]}));
            }

            //Move the cell to be copied to the working cell & all targets
            output.append(ADD.compile(args));

            //Move it back from the working cell to the original if it was a reference.
            if (args[0].charAt(0)=='@') {
                output.append(ADD.compile(new String[] {"@"+args[args.length-1],args[0].substring(1)}));
            }

            return output.toString();
        }
    };

    /**
     * Moves one cell into all the given targets.
     * DESTROYS ORIGINAL
     * e.g.
     * MOV @2 3 4;
     * Moves cell 2 into cells 3 & 4.
     */
    private EARInstruction MOV = new EARInstruction(
            "MOV\\s+(@|@-)?\\d+\\s+(-?\\d+\\s+)*-?\\d+\\s*") {
        public String compile(String[] args) {
            StringBuilder output = new StringBuilder();

            //Zero all target cells
            for (int i=1; i<args.length; i++) {
                output.append(ZERO.compile(new String[]{args[i]}));
            }

            //Move the cell to be copied to the working cell & all targets
            output.append(ADD.compile(args));

            return output.toString();
        }
    };
}
