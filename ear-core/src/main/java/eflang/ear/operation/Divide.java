package eflang.ear.operation;

import com.google.common.collect.ImmutableList;
import eflang.ear.core.Argument;
import eflang.ear.core.ArgumentValidator;
import eflang.ear.core.EARException;
import eflang.ear.core.Instruction;

import java.util.ArrayList;
import java.util.List;

public class Divide implements Operation {
    @Override
    public List<Instruction> compile(List<Argument> args) {
        Argument numerator = args.get(0);
        Argument denominator = args.get(1);
        Argument tgt = args.get(2);
        Argument flag = args.get(3);
        Argument w1 = args.get(4);
        Argument w2 = args.get(5);
        Argument w3 = args.get(6);
        Argument w4 = args.get(7);
        Argument w5 = args.get(8);

        if ((numerator.getType() == Argument.Type.CONSTANT) && (denominator.getType() == Argument.Type.CONSTANT)) {
            return constDivideConst(numerator.getValue(), denominator.getValue(), tgt.getValue());
        } else {
            List<Instruction> instructions = new ArrayList<>();

            // If one of them is a const, put it in w1 and do the cell-cell algorithm.
            if (numerator.getType() == Argument.Type.CONSTANT) {
                instructions.addAll((new Move()).compile(numerator, w1));
                numerator = w1;
            } else if (denominator.getType() == Argument.Type.CONSTANT) {
                instructions.addAll((new Move()).compile(denominator, w1));
                denominator = w1;
            }

            // Now do the division.
            instructions.addAll(cellDivideCell(
                    numerator.getValue(), denominator.getValue(), tgt.getValue(),
                    w2.getValue(), w3.getValue(), w4.getValue(), w5.getValue()));

            return instructions;
        }
    }

    private List<Instruction> cellDivideCell(
            int numerator, int denominator, int tgt, int denominator_backup, int hitZeroFlag, int w4, int w5) {
        List<Instruction> instructions = new ArrayList<>();

        // Zero the target cell.
        instructions.addAll((new Zero()).compile(Argument.constant(tgt)));

        // Zero all working cells.
        instructions.addAll((new Zero()).compile(Argument.constant(denominator_backup)));
        instructions.addAll((new Zero()).compile(Argument.constant(hitZeroFlag)));
        instructions.addAll((new Zero()).compile(Argument.constant(w4)));
        instructions.addAll((new Zero()).compile(Argument.constant(w5)));

        // Store denominator in denominator_backup.
        instructions.addAll((new Copy()).compile(
                Argument.cell(denominator),
                Argument.constant(denominator_backup),
                Argument.constant(w4)
        ));

        // While numerator not 0.
        instructions.addAll((new While()).compile(Argument.cell(numerator)));

        // Keep a flag which will tell us if one of the cells hits 0 so we can break the loop without going negative.
        instructions.addAll((new Move()).compile(Argument.constant(1), Argument.constant(hitZeroFlag)));

        instructions.addAll((new While()).compile(Argument.cell(hitZeroFlag)));

        // Subtract one from each.
        instructions.addAll((new Subtract()).compile(
                Argument.constant(1),
                Argument.cell(numerator),
                Argument.cell(denominator)
        ));

        // === If numerator hit 0, set flag so we break out.
        instructions.addAll((new Copy()).compile(
                Argument.cell(numerator),
                Argument.constant(w4),
                Argument.constant(w5)
        ));

        // This loop sets w5 to 1 if numerator was 0, and 0 otherwise.
        instructions.addAll((new Move()).compile(Argument.constant(1), Argument.constant(w5)));
        instructions.addAll((new While()).compile(Argument.cell(w4)));
        instructions.addAll((new Zero()).compile(Argument.cell(w5)));
        instructions.addAll((new Zero()).compile(Argument.cell(w4)));
        instructions.addAll((new EndWhile()).compile());

        // This loop uses that to switch off the hitZeroFlag only if the numerator was 0.
        instructions.addAll((new While()).compile(Argument.cell(w5)));
        instructions.addAll((new Zero()).compile(Argument.cell(hitZeroFlag)));
        instructions.addAll((new Zero()).compile(Argument.cell(w5)));
        instructions.addAll((new EndWhile()).compile());

        // === If denominator hit 0, set flag so we break out, increment answer counter and refill denominator.
        instructions.addAll((new Copy()).compile(
                Argument.cell(denominator),
                Argument.constant(w4),
                Argument.constant(w5)
        ));

        // This loop sets w5 to 1 if denominator was 0, and 0 otherwise.
        instructions.addAll((new Move()).compile(Argument.constant(1), Argument.constant(w5)));
        instructions.addAll((new While()).compile(Argument.cell(w4)));
        instructions.addAll((new Zero()).compile(Argument.cell(w5)));
        instructions.addAll((new Zero()).compile(Argument.cell(w4)));
        instructions.addAll((new EndWhile()).compile());

        // This loop uses that to set the flag, increment the answer and reset the denominator if the numerator was 0.
        instructions.addAll((new While()).compile(Argument.cell(w5)));
        instructions.addAll((new Zero()).compile(Argument.cell(hitZeroFlag)));
        instructions.addAll((new Zero()).compile(Argument.cell(w5)));
        instructions.addAll((new Add()).compile(Argument.constant(1), Argument.constant(tgt)));
        instructions.addAll((new Copy()).compile(
                Argument.cell(denominator_backup),
                Argument.constant(denominator),
                Argument.constant(w4)
        ));
        instructions.addAll((new EndWhile()).compile());

        instructions.addAll((new EndWhile()).compile());

        instructions.addAll((new EndWhile()).compile());

        return instructions;
    }

    private List<Instruction> constDivideConst(int numerator, int denominator, int tgt) {
        return (new Move()).compile(ImmutableList.of(
                Argument.constant(numerator / denominator),
                Argument.constant(tgt)
        ));
    }

    @Override
    public void validateArgs(List<Argument> args) throws EARException {
        Argument.validator()
                .one(ArgumentValidator.Type.EITHER)
                .one(ArgumentValidator.Type.EITHER)
                .one(ArgumentValidator.Type.CONSTANT)
                .one(ArgumentValidator.Type.CONSTANT)
                .one(ArgumentValidator.Type.CONSTANT)
                .one(ArgumentValidator.Type.CONSTANT)
                .one(ArgumentValidator.Type.CONSTANT)
                .one(ArgumentValidator.Type.CONSTANT)
                .one(ArgumentValidator.Type.CONSTANT)
                .validate(args);
    }

    public String toString() {
        return "DIV";
    }
}
