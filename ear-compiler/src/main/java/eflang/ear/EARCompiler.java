package eflang.ear;

import eflang.ear.composer.Composer;
import eflang.ear.composer.OnlyRunsComposer;
import eflang.ear.operation.*;

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
            String instruction = instructions[i].replaceAll(" +", " ");
            instruction = instruction.replaceAll("^ +", "");
            if (instruction.equals("")) {
                continue;
            }

            Command cmd = parseLine(instruction);
            String compiledCmd = compileCommand(cmd);

            //calculate how many commands into the EF code we are
            //Add it to the array of start positions
            lineStartPositions.add(numNotes);

            numNotes += compiledCmd.split(" ").length;

            output.append(compiledCmd);
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

            case REST:
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

    private String compileCommand(Command cmd) {
        return String.join(" ",
                cmd.getOperation().compile(cmd.getArguments()).stream()
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

    private static Command parseLine(String line) {
        List<String> tokens = Arrays.asList(line.split(" "));
        List<Argument> args = tokens.subList(1, tokens.size()).stream()
                .map(EARCompiler::parseArg)
                .collect(Collectors.toList());

        return Command.of(lookupOperation(tokens.get(0)), args);
    }

    private static Operation lookupOperation(String opCode) {
        switch (opCode) {
            case "ADD":
                return new Add();
            case "COPY":
                return new Copy();
            case "DIV":
                return new Divide();
            case "ENDWHILE":
                return new EndWhile();
            case "GOTO":
                return new Goto();
            case "IN":
                return new Input();
            case "MOV":
                return new Move();
            case "MUL":
                return new Multiply();
            case "OUT":
                return new Output();
            case "SUB":
                return new Subtract();
            case "WHILE":
                return new While();
            case "ZERO":
                return new Zero();
            default:
                throw new RuntimeException("Unknown opCode: " + opCode);
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
            return compileOperation(new Input(), parseArgs(args));
        }
    };

    /**
     * Outputs target cell.
     * e.g.
     * OUT 5;
     */
    private EARInstruction OUT = new EARInstruction("OUT\\s+-?\\d+\\s*") {
        public String compile(String[] args) {
            return compileOperation(new Output(), parseArgs(args));
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
            return compileOperation(new While(), parseArgs(args));
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
            return compileOperation(new EndWhile(), parseArgs(args));
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
            return compileOperation(new Add(), parseArgs(args));
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
            return compileOperation(new Subtract(), parseArgs(args));
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
            return compileOperation(new Multiply(), parseArgs(args));
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
            return compileOperation(new Divide(), parseArgs(args));
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
            return compileOperation(new Copy(), parseArgs(args));
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
            return compileOperation(new Move(), parseArgs(args));
        }
    };
}
