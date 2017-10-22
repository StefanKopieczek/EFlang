package eflang.ear.core;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class ArgumentValidatorTest {
    @Test
    void testNoArgsSuccess() {
        validates(Argument.validator(), ImmutableList.of());
    }

    @Test
    void testNoArgsFailsWhenPassedAnyArgs() {
        doesNotValidate(Argument.validator(), ImmutableList.of(Argument.constant(0)));
    }

    @Test
    void testOneCellValidates() {
        validates(Argument.validator().one(ArgumentValidator.Type.CELL), ImmutableList.of(Argument.cell(0)));
    }

    @Test
    void testOneConstantValidates() {
        validates(Argument.validator().one(ArgumentValidator.Type.CONSTANT), ImmutableList.of(Argument.constant(0)));
    }

    @Test
    void testConstDoestNotValidateCell() {
        doesNotValidate(Argument.validator().one(ArgumentValidator.Type.CONSTANT), ImmutableList.of(Argument.cell(0)));
    }

    @Test
    void testCellDoesNotValidateConst() {
        doesNotValidate(Argument.validator().one(ArgumentValidator.Type.CELL), ImmutableList.of(Argument.constant(0)));
    }

    @Test
    void testEitherValidatesCell() {
        validates(Argument.validator().one(ArgumentValidator.Type.EITHER), ImmutableList.of(Argument.cell(0)));
    }

    @Test
    void testEitherValidatesConst() {
        validates(Argument.validator().one(ArgumentValidator.Type.EITHER), ImmutableList.of(Argument.constant(0)));
    }

    @Test
    void testCellDoesNotValidateEmpty() {
        doesNotValidate(Argument.validator().one(ArgumentValidator.Type.CELL), ImmutableList.of());
    }

    @Test
    void testConstDoesNotValidateEmpty() {
        doesNotValidate(Argument.validator().one(ArgumentValidator.Type.CONSTANT), ImmutableList.of());
    }

    @Test
    void testEitherDoesNotValidateEmpty() {
        doesNotValidate(Argument.validator().one(ArgumentValidator.Type.EITHER), ImmutableList.of());
    }

    @Test
    void testMixedWithSuccess() {
        validates(
                Argument.validator()
                        .one(ArgumentValidator.Type.CELL)
                        .one(ArgumentValidator.Type.CONSTANT)
                        .one(ArgumentValidator.Type.CELL),
                ImmutableList.of(
                        Argument.cell(0),
                        Argument.constant(0),
                        Argument.cell(0)
                )
        );
    }

    @Test
    void testMixedWithTooFew() {
        doesNotValidate(
                Argument.validator()
                        .one(ArgumentValidator.Type.CELL)
                        .one(ArgumentValidator.Type.CONSTANT)
                        .one(ArgumentValidator.Type.CELL),
                ImmutableList.of(
                        Argument.cell(0),
                        Argument.constant(0)
                )
        );
    }

    @Test
    void testMixedWithTooMany() {
        doesNotValidate(
                Argument.validator()
                        .one(ArgumentValidator.Type.CELL)
                        .one(ArgumentValidator.Type.CONSTANT)
                        .one(ArgumentValidator.Type.CELL),
                ImmutableList.of(
                        Argument.cell(0),
                        Argument.constant(0),
                        Argument.cell(0),
                        Argument.cell(0)
                )
        );
    }

    @Test
    void testMixedWithWrongTypes() {
        doesNotValidate(
                Argument.validator()
                        .one(ArgumentValidator.Type.CELL)
                        .one(ArgumentValidator.Type.CONSTANT)
                        .one(ArgumentValidator.Type.CELL),
                ImmutableList.of(
                        Argument.cell(0),
                        Argument.constant(0),
                        Argument.constant(0)
                )
        );
    }

    @Test
    void testManyDoesNotValidateNone() {
        doesNotValidate(Argument.validator().many(ArgumentValidator.Type.CELL), ImmutableList.of());
    }

    @Test
    void testManyValidatesOne() {
        validates(Argument.validator().many(ArgumentValidator.Type.CELL), ImmutableList.of(Argument.cell(0)));
    }

    @Test
    void testManyValidatesMany() {
        validates(
                Argument.validator().many(ArgumentValidator.Type.CELL),
                ImmutableList.of(
                        Argument.cell(0),
                        Argument.cell(1),
                        Argument.cell(2)
                )
        );
    }

    @Test
    void testOneThenMany() {
        validates(
                Argument.validator().one(ArgumentValidator.Type.CELL).many(ArgumentValidator.Type.CONSTANT),
                ImmutableList.of(
                        Argument.cell(0),
                        Argument.constant(1),
                        Argument.constant(2),
                        Argument.constant(3)
                )
        );
    }

    @Test
    void testManyThenOne() {
        validates(
                Argument.validator().many(ArgumentValidator.Type.CELL).one(ArgumentValidator.Type.CONSTANT),
                ImmutableList.of(
                        Argument.cell(0),
                        Argument.cell(1),
                        Argument.cell(2),
                        Argument.constant(3)
                )
        );
    }

    @Test
    void testOneThenManyThenOne() {
        validates(
                Argument.validator()
                        .one(ArgumentValidator.Type.CONSTANT)
                        .many(ArgumentValidator.Type.CELL)
                        .one(ArgumentValidator.Type.CONSTANT),
                ImmutableList.of(
                        Argument.constant(3),
                        Argument.cell(0),
                        Argument.cell(1),
                        Argument.cell(2),
                        Argument.constant(3)
                )
        );
    }

    private void validates(ArgumentValidator validator, List<Argument> args) {
        try {
            validator.validate(args);
        } catch (EARInvalidSignatureException e) {
            fail("Failed validation", e);
        }
    }

    private void doesNotValidate(ArgumentValidator validator, List<Argument> args) {
        try {
            validator.validate(args);
            fail("Should have failed validation");
        } catch (EARInvalidSignatureException e) {
        }
    }
}
