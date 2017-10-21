package eflang.ear.operation;

import com.google.common.collect.ImmutableList;
import eflang.ear.Argument;
import eflang.ear.Instruction;

import java.util.ArrayList;
import java.util.List;

public class Multiply implements Operation {
    @Override
    public List<Instruction> compile(List<Argument> args) {
        assert args.size() == 4;

        Argument a0 = args.get(0);
        Argument a1 = args.get(1);
        Argument tgt = args.get(2);
        Argument w = args.get(3);

        Argument.Type t0 = a0.getType();
        Argument.Type t1 = a1.getType();

        if ((t0 == Argument.Type.CONSTANT) && (t1 == Argument.Type.CONSTANT)) {
            return constTimesConst(a0.getValue(), a1.getValue(), tgt.getValue());
        } else if ((t0 == Argument.Type.CELL) && (t1 == Argument.Type.CELL)) {
            return cellTimesCell(a0.getValue(), a1.getValue(), tgt.getValue(), w.getValue());
        } else {
            // One cell, one const.
            if (t0 == Argument.Type.CONSTANT) {
                return constTimesCell(a0.getValue(), a1.getValue(), tgt.getValue(), w.getValue());
            } else {
                return constTimesCell(a1.getValue(), a0.getValue(), tgt.getValue(), w.getValue());
            }
        }
    }

    private List<Instruction> constTimesConst(int x, int y, int tgt) {
        return (new Move()).compile(ImmutableList.of(
                Argument.constant(x * y),
                Argument.constant(tgt)
        ));
    }

    private List<Instruction> constTimesCell(int x, int c1, int tgt, int w) {
        // Unroll the loop.
        List<Instruction> instructions = new ArrayList<>();

        // Start by zeroing the target and working cells since we're going to be using ADD.
        instructions.addAll((new Zero()).compile(ImmutableList.of(Argument.constant(tgt))));
        instructions.addAll((new Zero()).compile(ImmutableList.of(Argument.constant(w))));

        for (int i = 0; i < x; i++) {
            instructions.addAll((new Add()).compile(ImmutableList.of(
                    Argument.cell(c1),
                    Argument.constant(tgt),
                    Argument.constant(w)
            )));

            if (i < x - 1) {
                instructions.addAll((new Add()).compile(ImmutableList.of(
                        Argument.cell(w),
                        Argument.constant(c1)
                )));
            }
        }

        return instructions;
    }

    private List<Instruction> cellTimesCell(int c1, int c2, int tgt, int w) {
        List<Instruction> instructions = new ArrayList<>();

        // Start by zeroing the target and working cells since we're going to be using ADD.
        instructions.addAll((new Zero()).compile(ImmutableList.of(Argument.constant(tgt))));
        instructions.addAll((new Zero()).compile(ImmutableList.of(Argument.constant(w))));

        // Repeatedly add one cell, whilst keeping track of how many adds to do by decrementing the other.
        instructions.add(Instruction.goTo(c1));
        instructions.add(Instruction.ensureSad());
        instructions.add(Instruction.startLoop());

        instructions.addAll((new Add()).compile(ImmutableList.of(
                Argument.cell(c2),
                Argument.constant(tgt),
                Argument.constant(w)
        )));

        instructions.addAll((new Add()).compile(ImmutableList.of(
                Argument.cell(w),
                Argument.constant(c2)
        )));

        instructions.add(Instruction.goTo(c1));
        instructions.add(Instruction.decrement());
        instructions.add(Instruction.endLoop());

        return instructions;
    }
}
