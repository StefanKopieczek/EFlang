package eflang.ear.core;

import eflang.ear.composer.Composer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class StatefulInstructionCompiler {
    public Composer composer;
    private int p; //Cell pointer
    private String currentNote;
    private int optimism;
    private Stack<java.lang.Integer> branchLocStack;
    private Stack<java.lang.String> branchNoteStack;
    private Stack<java.lang.Integer> branchOptimismStack;

    public StatefulInstructionCompiler(Composer composer) {
        this.composer = composer;
    }

    public void resetState() {
        p = 0;
        currentNote = composer.getStartingNode();
        optimism = 0;
        branchLocStack = new Stack<>();
        branchNoteStack = new Stack<>();
        branchOptimismStack = new Stack<>();
        new ArrayList<>();
    }

    public String getStartingNote() {
        return composer.getStartingNode();
    }

    public List<String> compileInstruction(Instruction instruction) {
        List<String> output = new ArrayList<>();
        switch (instruction.getType()) {
            case GOTO:
                output.addAll(goTo(instruction.getValue()));
                break;

            case INCREMENT:
                output.addAll(increment());
                break;

            case DECREMENT:
                output.addAll(decrement());
                break;

            case START_LOOP:
                branchLocStack.push(p);
                branchNoteStack.push(currentNote);
                branchOptimismStack.push(optimism);
                output.add("(");
                break;

            case END_LOOP:
                // Ensure same pointer location, optimism and note as start of loop.
                // The order here is important due to the guarantees each of these operations provides.
                output.addAll(goTo(branchLocStack.pop()));
                output.addAll(setOptimism(branchOptimismStack.pop()));
                output.addAll(changeNoteTo(branchNoteStack.pop()));
                output.add(")");
                break;

            case ENSURE_HAPPY:
                output.addAll(ensureHappy());
                break;

            case ENSURE_SAD:
                output.addAll(ensureSad());
                break;

            case REST:
                output.add("r");
                break;
        }
        return output;
    }

    /**
     * Safely moves the pointer one to the left.
     *
     * @return The EF code to make that happen.
     */
    private List<String> moveLeft() {
        List<String> output = new ArrayList<>();

        //Decrement pointer
        p--;
        //Set optimisim
        optimism = -1;

        if (currentNote.equals(composer.bottomNote())) {
            // Oops, we were already at the bottom, so jump up at least 2 and then move left to compensate.
            // Do it carefully by stepping, using lowerNote could fail if we hit the bottom.
            currentNote = composer.higherNote(composer.nextNote(currentNote));
            output.add(currentNote);
            currentNote = composer.prevNote(currentNote);
            output.add(currentNote);
        }

        currentNote = composer.lowerNote(currentNote);
        output.add(currentNote);
        return output;
    }

    /**
     * Safely moves the pointer one to the right.
     *
     * @return The EF code to make that happen.
     */
    private List<String> moveRight() {
        List<String> output = new ArrayList<>();

        //Increment pointer
        p++;
        //Set optimisim
        optimism = 1;

        if (currentNote.equals(composer.topNote())) {
            // Oops, we were already at the top, so jump down to the bottom and then move right to compensate.
            // Do it carefully by stepping, using higherNote could fail if we hit the bottom.
            currentNote = composer.lowerNote(composer.prevNote(currentNote));
            output.add(currentNote);
            currentNote = composer.nextNote(currentNote);
            output.add(currentNote);
        }

        currentNote = composer.higherNote(currentNote);
        output.add(currentNote);
        return output;
    }

    private List<String> goTo(int cell) {
        List<String> output = new ArrayList<>();
        while (p < cell) {
            output.addAll(moveRight());
        }
        while (p > cell) {
            output.addAll(moveLeft());
        }
        return output;
    }

    private List<String> ensureHappy() {
        return setOptimism(1);
    }

    private List<String> ensureSad() {
        return setOptimism(-1);
    }

    /**
     * Set optimism without changing pointer location.
     * Note may change.
     */
    private List<String> setOptimism(int targetOptimism) {
        List<String> output = new ArrayList<>();

        if (optimism < targetOptimism) {
            // Need to get happy.
            output.addAll(moveLeft());
            output.addAll(moveRight());
        }

        if (optimism > targetOptimism) {
            // Need to get sad.
            output.addAll(moveRight());
            output.addAll(moveLeft());
        }

        return output;
    }

    /**
     * Safely adds one to current cell
     *
     * @return The EF code to make it happen
     */
    private List<String> increment() {
        List<String> output = new ArrayList<>();
        output.addAll(ensureHappy());
        output.add(currentNote);
        return output;
    }

    /**
     * Safely adds one to current cell
     *
     * @return The EF code to make it happen
     */
    private List<String> decrement() {
        List<String> output = new ArrayList<>();
        output.addAll(ensureSad());
        output.add(currentNote);
        return output;
    }

    /**
     * Changes current note to specified target note
     * without changing the pointer/optimism
     *
     * @param target note to change to
     * @return compiled EF code
     */
    private List<String> changeNoteTo(String target) {
        List<String> output = new ArrayList<>();
        int targetOptimism = optimism;

        if (currentNote.equals(target)) {
            return output;
        }
        //Move note away from ends
        if (currentNote.equals(composer.bottomNote())) {
            currentNote = composer.nextNote(composer.nextNote(currentNote));
            output.add(currentNote);
            currentNote = composer.prevNote(currentNote);
            output.add(currentNote);
            optimism = -1;
        }
        if (currentNote.equals(composer.topNote())) {
            currentNote = composer.prevNote(composer.prevNote(currentNote));
            output.add(currentNote);
            currentNote = composer.nextNote(currentNote);
            output.add(currentNote);
            optimism = 1;
        }

        int noteCompare = composer.compareNotes(currentNote, target);
        if (noteCompare < 0) {
            output.add(composer.prevNote(currentNote));
            output.add(target);
            currentNote = target;
            optimism = 1;
        } else if (noteCompare > 0) {
            output.add(composer.nextNote(currentNote));
            output.add(target);
            currentNote = target;
            optimism = -1;
        }

        // Manually restore optimism if necessary.
        if (optimism < targetOptimism) {
            // Need to get happy.
            output.addAll(moveLeft());
            output.add(target);
            p += 1;
        } else if (optimism > targetOptimism) {
            // Need to get sad.
            output.addAll(moveRight());
            output.add(target);
            p -= 1;
        }
        optimism = targetOptimism;
        currentNote = target;

        return output;
    }
}