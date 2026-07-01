package de.tub.pes.syscir.analysis.abstraction_refinement;

import java.util.function.BiFunction;
import java.util.function.Function;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;

public class LogicTracker implements AbstractedLogic {

    private final AbstractedLogic base;
    private final BiFunction<AbstractedValue, AbstractedValue[], AbstractedValue> wrap;
    private final Function<AbstractedValue, AbstractedValue> unwrap;

    public LogicTracker(AbstractedLogic baseLogic, BiFunction<AbstractedValue, AbstractedValue[], AbstractedValue> wrap,
            Function<AbstractedValue, AbstractedValue> unwrap) {
        this.base = baseLogic;
        this.wrap = wrap;
        this.unwrap = unwrap;
    }

    private AbstractedValue unwrap(AbstractedValue value) {
        return this.unwrap.apply(value);
    }

    private AbstractedValue wrap(AbstractedValue value, AbstractedValue... sources) {
        return this.wrap.apply(value, sources);
    }

    @Override
    public AbstractedValue unknown(AbstractedValue... sources) {
        return wrap(base.unknown(sources), sources);
    }

    @Override
    public AbstractedValue value(Object value) {
        return wrap(base.value(value));
    }

    @Override
    public AbstractedValue union(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.union(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue abstractUnary(AbstractedValue value) {
        return wrap(base.abstractUnary(unwrap(value)), value);
    }

    @Override
    public AbstractedValue not(AbstractedValue value) {
        return wrap(base.not(unwrap(value)), value);
    }

    @Override
    public AbstractedValue negative(AbstractedValue value) {
        return wrap(base.negative(unwrap(value)), value);
    }

    @Override
    public AbstractedValue bitwiseNot(AbstractedValue value) {
        return wrap(base.bitwiseNot(unwrap(value)), value);
    }

    @Override
    public AbstractedValue abstractBinary(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.abstractBinary(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue and(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.and(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue or(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.or(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue xor(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.xor(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue equal(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.equal(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue unequal(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.unequal(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue lessThan(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.lessThan(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue lessThanOrEqual(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.lessThanOrEqual(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue greaterThan(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.greaterThan(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue greaterThanOrEqual(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.greaterThanOrEqual(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue add(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.add(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue increment(AbstractedValue value) {
        return wrap(base.increment(unwrap(value)), value);
    }

    @Override
    public AbstractedValue subtract(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.subtract(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue decrement(AbstractedValue value) {
        return wrap(base.decrement(unwrap(value)), value);
    }

    @Override
    public AbstractedValue multiply(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.multiply(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue divide(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.divide(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue modulo(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.modulo(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue bitwiseAnd(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.bitwiseAnd(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue bitwiseOr(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.bitwiseOr(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue shiftBitsRight(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.shiftBitsRight(unwrap(value1), unwrap(value2)), value1, value2);
    }

    @Override
    public AbstractedValue shiftBitsLeft(AbstractedValue value1, AbstractedValue value2) {
        return wrap(base.shiftBitsLeft(unwrap(value1), unwrap(value2)), value1, value2);
    }

}
