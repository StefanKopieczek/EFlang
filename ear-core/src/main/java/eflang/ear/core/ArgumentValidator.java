package eflang.ear.core;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;

import java.util.ArrayList;
import java.util.List;

public class ArgumentValidator {
    List<ArgSpec> argSpecs;

    ArgumentValidator() {
        this.argSpecs = new ArrayList<>();
    }

    public void validate(List<Argument> args) throws EARInvalidSignatureException {
        // Copy the args so we don't destroy the input list.
        args = Lists.newArrayList(args);

        int manyIndex = Iterables.indexOf(argSpecs, spec -> spec.arity == Arity.MANY);
        if (manyIndex == -1) {
            // No MANY, just simple validate.
            simpleValidate(argSpecs, args);
            return;
        }

        // Check we have enough args for this to even make sense.
        if (args.size() <= manyIndex) {
            throw new EARInvalidSignatureException("Too few args");
        }

        // Validate stuff before the MANY.
        simpleValidate(argSpecs.subList(0, manyIndex), args.subList(0, manyIndex));

        // Validate stuff after the MANY if any.
        List<ArgSpec> afterMany = argSpecs.subList(manyIndex + 1, argSpecs.size());
        simpleValidate(afterMany, args.subList(args.size() - afterMany.size(), args.size()));

        // Validate the MANY.
        ArgSpec manySpec = argSpecs.get(manyIndex);
        List<Argument> manyArgs = args.subList(manyIndex, args.size() - afterMany.size());
        if (!manyArgs.stream().allMatch(arg -> manySpec.matchType(arg.getType()))) {
            throw new EARInvalidSignatureException("Wrong argument types");
        }
    }

    private void simpleValidate(List<ArgSpec> specs, List<Argument> args) throws EARInvalidSignatureException {
        if (specs.stream().anyMatch(spec -> spec.arity == Arity.MANY)) {
            throw new RuntimeException("Can't pass MANY spec to simpleValidate");
        }

        if (specs.size() != args.size()) {
            throw new EARInvalidSignatureException("Wrong number of args");
        }

        if (!Streams.zip(specs.stream(), args.stream(), (spec, arg) -> spec.matchType(arg.getType()))
                .allMatch(x -> x)) {
            throw new EARInvalidSignatureException("Wrong argument types");
        }
    }

    public ArgumentValidator one(Type type) {
        argSpecs.add(new ArgSpec(type, Arity.ONE));
        return this;
    }

    public ArgumentValidator many(Type type) {
        if (argSpecs.stream().anyMatch(spec -> spec.arity == Arity.MANY)) {
            throw new RuntimeException("Argument list may only contain one varargs");
        }
        argSpecs.add(new ArgSpec(type, Arity.MANY));
        return this;
    }

    private class ArgSpec {
        Type type;
        Arity arity;

        ArgSpec(Type type, Arity arity) {
            this.type = type;
            this.arity = arity;
        }

        boolean matchType(Argument.Type argType) {
            switch (type) {
                case EITHER:
                    return true;
                case CELL:
                    return argType == Argument.Type.CELL;
                case CONSTANT:
                    return argType == Argument.Type.CONSTANT;
                default:
                    return false;
            }
        }
    }

    private enum Arity {
        ONE,
        MANY
    }

    public enum Type {
        CELL,
        CONSTANT,
        EITHER
    }
}
