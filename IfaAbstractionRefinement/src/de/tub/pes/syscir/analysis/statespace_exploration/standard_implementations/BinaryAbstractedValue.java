package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ExpressionCrawler.NullConstant;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Simple implementation of {@link AbstractedValue} that either knows the exact value or doesn't
 * know anything.
 * 
 * @author Jonas Becker-Kupczok, Lukas Ernst
 */
public class BinaryAbstractedValue implements AbstractedValue {
    
    private static IllegalArgumentException incompatibleType(String operation, AbstractedValue value) {
        return new IllegalArgumentException("Incompatible type for " + operation + ": " + value.get().getClass());
    }
    
    private static IllegalArgumentException incompatibleTypes(String operation, AbstractedValue value1,
            AbstractedValue value2) {
        return new IllegalArgumentException("Incompatible types for " + operation + ": " + value1.get().getClass()
                + " and " + value2.get().getClass());
    }
    
    public static class BinaryAbstractedLogic implements AbstractedLogic {
        
        public static final BinaryAbstractedLogic INSTANCE = new BinaryAbstractedLogic();
        
        private BinaryAbstractedLogic() {}
        
        @Override
        public AbstractedValue unknown(AbstractedValue... sources) {
            return UNKNOWN;
        }
        
        @Override
        public AbstractedValue value(Object value) {
            return of(value);
        }
        
        @Override
        public AbstractedValue union(AbstractedValue v1, AbstractedValue v2) {
            AbstractedValue equal = equal(v1, v2);
            return equal.isDetermined() && Boolean.TRUE.equals(equal.get()) ? v1 : UNKNOWN;
        }
        
        @Override
        public AbstractedValue abstractUnary(AbstractedValue value) {
            return UNKNOWN;
        }
        
        @Override
        public AbstractedValue not(AbstractedValue value) {
            return value.isDetermined() ? of(!(boolean) value.get()) : UNKNOWN;
        }
        
        @Override
        public AbstractedValue negative(AbstractedValue value) {
            if (!value.isDetermined()) {
                return UNKNOWN;
            }
            if (value.get() instanceof Integer i) {
                return of(-i);
            }
            if (value.get() instanceof Double d) {
                return of(-d);
            }
            throw incompatibleType("negative", value);
        }
        
        @Override
        public AbstractedValue bitwiseNot(AbstractedValue value) {
            return value.isDetermined() ? of(~(int) value.get()) : UNKNOWN;
        }
        
        @Override
        public AbstractedValue abstractBinary(AbstractedValue value1, AbstractedValue value2) {
            return UNKNOWN;
        }
        
        @Override
        public AbstractedValue and(AbstractedValue v1, AbstractedValue v2) {
            if (v1.isDetermined() && !(boolean) v1.get()) {
                return of(false);
            }
            if (v2.isDetermined() && !(boolean) v2.get()) {
                return of(false);
            }
            return v1.isDetermined() && v2.isDetermined() ? of(true) : UNKNOWN;
        }
        
        @Override
        public AbstractedValue or(AbstractedValue v1, AbstractedValue v2) {
            if (v1.isDetermined() && (boolean) v1.get()) {
                return of(true);
            }
            if (v2.isDetermined() && (boolean) v2.get()) {
                return of(true);
            }
            return v1.isDetermined() && v2.isDetermined() ? of(false) : UNKNOWN;
        }
        
        @Override
        public AbstractedValue xor(AbstractedValue v1, AbstractedValue v2) {
            if (!v1.isDetermined() || !v2.isDetermined()) {
                return UNKNOWN;
            }
            if (v1.get() instanceof Boolean b1 && v2.get() instanceof Boolean b2) {
                return of(b1.booleanValue() != b2.booleanValue());
            }
            if (v1.get() instanceof Integer i1 && v2.get() instanceof Integer i2) {
                return of(i1.intValue() ^ i2.intValue());
            }
            throw incompatibleTypes("xor", v1, v2);
        }
        
        @Override
        public AbstractedValue equal(AbstractedValue v1, AbstractedValue v2) {
            if (!v1.isDetermined() || !v2.isDetermined()) {
                return UNKNOWN;
            }
            
            Object o1 = v1.get();
            Object o2 = v2.get();
            
            if (o1 == null || o2 == null || o1 == NullConstant.INSTANCE || o2 == NullConstant.INSTANCE) {
                return of(o1 == o2);
            }
            
            if (o1 instanceof Boolean && o2 instanceof Boolean) {
                return of(o1.equals(o2));
            }
            
            if (o1 instanceof Number n1 && o2 instanceof Number n2) {
                return of(n1.doubleValue() == n2.doubleValue());
            }
            
            return UNKNOWN;
        }

        @Override
        public AbstractedValue unequal(AbstractedValue value1, AbstractedValue value2) {
            return not(equal(value1, value2));
        }

        private AbstractedValue binaryNumberOp(AbstractedValue value1, AbstractedValue value2, String opName,
                BiFunction<Number, Number, Object> op) {
            if (!value1.isDetermined() || !value2.isDetermined()) {
                return UNKNOWN;
            }
            if (value1.get() instanceof Number n1 && value2.get() instanceof Number n2) {
                return of(op.apply(n1, n2));
            }
            throw incompatibleTypes(opName, value1, value2);
        }
        
        private AbstractedValue binaryNumberOp(AbstractedValue value1, AbstractedValue value2, String opName,
                BiFunction<Integer, Integer, Object> intOp, BiFunction<Double, Double, Object> doubleOp) {
            return binaryNumberOp(value1, value2, opName, (n1, n2) -> {
                if (n1 instanceof Integer i1 && n2 instanceof Integer i2) {
                    return intOp.apply(i1, i2);
                }
                if (doubleOp == null) {
                    throw incompatibleTypes(opName, value1, value2);
                }
                return doubleOp.apply(n1.doubleValue(), n2.doubleValue());
            });
        }
        
        /**
         * This method is used to apply a unary operation on a number.
         * 
         * For example "++" or "--".
         * 
         * @param value AbstractedValue that the operation is applied on.
         * @param opName Name of the operation.
         * @param op Function that is applied to the value.
         * @return Value after the operation has been applied.
         */
        private AbstractedValue unaryNumberOp(AbstractedValue value, String opName, Function<Integer, Object> op) {
            if (!value.isDetermined()) {
                return UNKNOWN;
            }
            if (value.get() instanceof Integer n1) {
                return of(op.apply(n1));
            }
            throw incompatibleType(opName, value);
        }
        
        @Override
        public AbstractedValue lessThan(AbstractedValue value1, AbstractedValue value2) {
            return binaryNumberOp(value1, value2, "<", (i1, i2) -> i1 < i2, (d1, d2) -> d1 < d2);
        }
        
        @Override
        public AbstractedValue lessThanOrEqual(AbstractedValue value1, AbstractedValue value2) {
            return binaryNumberOp(value1, value2, "<=", (i1, i2) -> i1 <= i2, (d1, d2) -> d1 <= d2);
        }
        
        @Override
        public AbstractedValue greaterThan(AbstractedValue value1, AbstractedValue value2) {
            return binaryNumberOp(value1, value2, ">", (i1, i2) -> i1 > i2, (d1, d2) -> d1 > d2);
        }
        
        @Override
        public AbstractedValue greaterThanOrEqual(AbstractedValue value1, AbstractedValue value2) {
            return binaryNumberOp(value1, value2, ">=", (i1, i2) -> i1 >= i2, (d1, d2) -> d1 >= d2);
        }
        
        @Override
        public AbstractedValue add(AbstractedValue value1, AbstractedValue value2) {
            return binaryNumberOp(value1, value2, "+", (i1, i2) -> i1 + i2, (d1, d2) -> d1 + d2);
        }
        
        @Override
        public AbstractedValue increment(AbstractedValue value) {
            return unaryNumberOp(value, "++", (i1) -> i1 + 1);
        }
        
        @Override
        public AbstractedValue subtract(AbstractedValue value1, AbstractedValue value2) {
            return binaryNumberOp(value1, value2, "-", (i1, i2) -> i1 - i2, (d1, d2) -> d1 - d2);
        }
        
        @Override
        public AbstractedValue decrement(AbstractedValue value) {
            return unaryNumberOp(value, "--", (i1) -> i1 - 1);
        }
        
        @Override
        public AbstractedValue multiply(AbstractedValue value1, AbstractedValue value2) {
            return binaryNumberOp(value1, value2, "*", (i1, i2) -> i1 * i2, (d1, d2) -> d1 * d2);
        }
        
        @Override
        public AbstractedValue divide(AbstractedValue value1, AbstractedValue value2) {
            return binaryNumberOp(value1, value2, "/", (i1, i2) -> i1 / i2, (d1, d2) -> d1 / d2);
        }
        
        @Override
        public AbstractedValue modulo(AbstractedValue value1, AbstractedValue value2) {
            return binaryNumberOp(value1, value2, "%", (i1, i2) -> i1 % i2, (d1, d2) -> d1 % d2);
        }
        
        @Override
        public AbstractedValue bitwiseAnd(AbstractedValue value1, AbstractedValue value2) {
            return binaryNumberOp(value1, value2, "&", (i1, i2) -> i1 & i2, null);
        }
        
        @Override
        public AbstractedValue bitwiseOr(AbstractedValue value1, AbstractedValue value2) {
            return binaryNumberOp(value1, value2, "|", (i1, i2) -> i1 | i2, null);
        }
        
        @Override
        public AbstractedValue shiftBitsLeft(AbstractedValue value1, AbstractedValue value2) {
            return binaryNumberOp(value1, value2, "<<", (i1, i2) -> i1 << i2, null);
        }
        
        @Override
        public AbstractedValue shiftBitsRight(AbstractedValue value1, AbstractedValue value2) {
            return binaryNumberOp(value1, value2, ">>", (i1, i2) -> i1 >> i2, null);
        }
        
    }
    
    /**
     * Represents an abstracted value where the real value is unknown.
     */
    public static final BinaryAbstractedValue UNKNOWN = new BinaryAbstractedValue(false, null);
    
    private static final int EMPTY_VALUE_HASH = 1063647192; // randomly chosen
    
    /**
     * Returns an abstracted value representing the given parameter.
     * 
     * @param <X> the type of the parameter
     * @param value a value
     * @return an abstracted value of the given parameter
     */
    public static BinaryAbstractedValue of(Object value) {
        return new BinaryAbstractedValue(true, value);
    }
    
    private final boolean determined;
    private final Object value;
    
    protected BinaryAbstractedValue(boolean determined, Object value) {
        this.determined = determined;
        this.value = value;
        
        assert determined || value == null;
    }
    
    @Override
    public boolean isDetermined() {
        return this.determined;
    }
    
    @Override
    public boolean isUnknown() {
        return !this.determined;
    }
    
    @Override
    public Object get() {
        if (!this.determined) {
            throw new NoSuchElementException();
        }
        
        return this.value;
    }
    
    @Override
    public int hashCode() {
        return this.determined ? this.value.hashCode() : EMPTY_VALUE_HASH;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof BinaryAbstractedValue bav)) {
            return false;
        }
        return (!this.determined && !bav.determined)
                || (this.determined && bav.determined && Objects.equals(this.value, bav.value));
    }
    
    @Override
    public String toString() {
        return this.determined ? "\"" + Objects.toString(this.value) + "\"" : "?";
    }
    
}
