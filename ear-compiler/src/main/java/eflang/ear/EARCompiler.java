package eflang.ear;

import eflang.ear.composer.Composer;
import eflang.ear.composer.OnlyRunsComposer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Compiler for the EAR language. <br/>
 * Takes in EAR code and compiles to raw EF.
 * @author Ryan Norris
 *
 */
public class EARCompiler {
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
        p = 0;
        currentNote = composer.getStartingNode();
        optimism = 0;
        branchLocStack = new Stack<>();
        branchNoteStack = new Stack<>();
        branchOptimismStack = new Stack<>();
        lineStartPositions = new ArrayList<>();
    }

    /**
     * Compiles provided EARCode into an EF program
     *
     * @param EARCode full program code.
     * @return String containing EF program
     */
    public String compile(String EARCode) throws EARException {
        resetState();
        StringBuilder output = new StringBuilder();

        //Discard comments (OLD COMMENT STYLE = "\\([^\\)]*\\)")
        EARCode = EARCode.replaceAll("//.*\\n", "\n");

        //Split into individual instructions
        String[] instructions = EARCode.split("(\\r?\\n)+");

        // Keep track of how many EF notes we've output.
        int numNotes = 0;

        output.append(currentNote);
        output.append(" ");

        for (int i = 0; i < instructions.length; i++) {
            String instruction = instructions[i].replaceAll(" +", " ");
            instruction = instruction.replaceAll("^ +", "");
            if (instruction.equals("")) {
                continue;
            }

            Command cmd = EARParser.parseLine(instruction);
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
     *
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
     *
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
        while (p < cell) {
            output.append(moveRight());
        }
        while (p > cell) {
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
     *
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
     *
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
     *
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

    private String compileCommand(Command cmd) {
        return String.join(" ",
                cmd.getOperation().compile(cmd.getArguments()).stream()
                        .map(instruction -> compileInstruction(instruction))
                        .collect(Collectors.toList()));
    }
}
