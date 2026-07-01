package de.tub.pes.syscir.analysis.statespace_exploration;

import java.util.HashMap;

/**
 * Interface to implement arithmetic operations on AbstractedValues.
 * 
 * @author Jonas Becker-Kupczok, Lukas Ernst
 */
public interface AbstractedLogic {

    /**
     * Generically construct an unknown value. A caller is encouraged to provide
     * other values that the resulting value originates from if applicable.
     */
    AbstractedValue unknown(AbstractedValue... sources);

    /**
     * Construct an AbstractedValue that has the specified known value.
     */
    AbstractedValue value(Object value);

    /**
     * Returns an abstracted value representing that the actual value could be any value allowed by the
     * specified abstracted values.
     *
     * @param the abstracted values
     * @return the least upper bound of the abstracted values
     */
    default AbstractedValue union(AbstractedValue... values) {
        if (values.length <= 0)
            throw new IllegalArgumentException("Zero elements for union");
        AbstractedValue out = values[0];
        if (values.length == 1)
            return out;
        for (int i = 1; i < values.length; i++)
            out = union(out, values[i]);
        return out;
    }

    AbstractedValue union(AbstractedValue value1, AbstractedValue value2);

    AbstractedValue abstractUnary(AbstractedValue value);

    AbstractedValue not(AbstractedValue value);

    AbstractedValue negative(AbstractedValue value);

    AbstractedValue bitwiseNot(AbstractedValue value);

    AbstractedValue abstractBinary(AbstractedValue value1, AbstractedValue value2);

    AbstractedValue and(AbstractedValue value1, AbstractedValue value2);

    AbstractedValue or(AbstractedValue value1, AbstractedValue value2);

    AbstractedValue xor(AbstractedValue value1, AbstractedValue value2);

    /**
     * Checks whether or not two abstracted values are guaranteed to be equal, unequal or whether no
     * definitive answer can be given.
     *
     * @param v1 an abstracted value
     * @param v2 another abstracted value
     * @return the equality between these two values as an abstracted value
     */
    AbstractedValue equal(AbstractedValue value1, AbstractedValue value2);

    AbstractedValue unequal(AbstractedValue value1, AbstractedValue value2);

    AbstractedValue lessThan(AbstractedValue value1, AbstractedValue value2);

    AbstractedValue lessThanOrEqual(AbstractedValue value1, AbstractedValue value2);

    AbstractedValue greaterThan(AbstractedValue value1, AbstractedValue value2);

    AbstractedValue greaterThanOrEqual(AbstractedValue value1, AbstractedValue value2);

    AbstractedValue add(AbstractedValue value1, AbstractedValue value2);

    AbstractedValue increment(AbstractedValue value);

    AbstractedValue subtract(AbstractedValue value1, AbstractedValue value2);

    AbstractedValue decrement(AbstractedValue value);

    AbstractedValue multiply(AbstractedValue value1, AbstractedValue value2);

    AbstractedValue divide(AbstractedValue value1, AbstractedValue value2);

    AbstractedValue modulo(AbstractedValue value1, AbstractedValue value2);

    AbstractedValue bitwiseAnd(AbstractedValue value1, AbstractedValue value2);

    AbstractedValue bitwiseOr(AbstractedValue value1, AbstractedValue value2);

    AbstractedValue shiftBitsRight(AbstractedValue value1, AbstractedValue value2);

    AbstractedValue shiftBitsLeft(AbstractedValue value1, AbstractedValue value2);

    /**
     * Enum of unary operations that may be queried by an operator symbol string and
     * can apply the operation using any AbstractedLogic.
     */
    public static enum UnaryOperation {

        ABSTRACT(null, AbstractedLogic::abstractUnary),
        NOT("!", AbstractedLogic::not),
        NEGATIVE("-", AbstractedLogic::negative),
        BITWISE_NOT("~", AbstractedLogic::bitwiseNot),
        INCREMENT("++", AbstractedLogic::increment),
        DECREMENT("--", AbstractedLogic::decrement);

        private static final HashMap<String, UnaryOperation> operations = new HashMap<>();
        static {
            for (UnaryOperation op : UnaryOperation.values()) {
                if (op.symbol != null)
                    operations.put(op.symbol, op);
            }
        }

        public final String symbol;
        private final UnaryOperationApplication apply;

        private UnaryOperation(String symbol, UnaryOperationApplication apply) {
            this.symbol = symbol;
            this.apply = apply;
        }

        public AbstractedValue apply(AbstractedLogic logic, AbstractedValue value) {
            return this.apply.apply(logic, value);
        }

        public static UnaryOperation get(String symbol) {
            return operations.getOrDefault(symbol, ABSTRACT);
        }
    }

    public static interface UnaryOperationApplication {
        AbstractedValue apply(AbstractedLogic logic, AbstractedValue value);
    }

    /**
     * Enum of binary operations that may be queried by an operator symbol string
     * and can apply the operation using any AbstractLogic.
     */
    public static enum BinaryOperation {

        ABSTRACT(null, AbstractedLogic::abstractBinary),
        AND("&&", AbstractedLogic::and),
        OR("||", AbstractedLogic::or),
        XOR("^", AbstractedLogic::xor),
        EQUAL("==", AbstractedLogic::equal),
        UNEQUAL("!=", AbstractedLogic::unequal),
        LESS_THAN("<", AbstractedLogic::lessThan),
        LESS_THAN_OR_EQUAL("<=", AbstractedLogic::lessThanOrEqual),
        GREATER_THAN(">", AbstractedLogic::greaterThan),
        GREATER_THAN_OR_EQUAL(">=", AbstractedLogic::greaterThanOrEqual),
        ADD("+", AbstractedLogic::add),
        SUBTRACT("-", AbstractedLogic::subtract),
        MULTIPLY("*", AbstractedLogic::multiply),
        DIVIDE("/", AbstractedLogic::divide),
        MODULO("%", AbstractedLogic::modulo),
        BITWISE_AND("&", AbstractedLogic::bitwiseAnd),
        BITWISE_OR("|", AbstractedLogic::bitwiseOr),
        SHIFT_BITS_RIGHT(">>", AbstractedLogic::shiftBitsRight),
        SHIFT_BITS_LEFT("<<", AbstractedLogic::shiftBitsLeft);

        private static final HashMap<String, BinaryOperation> operations = new HashMap<>();
        static {
            for (BinaryOperation op : BinaryOperation.values()) {
                if (op.symbol != null)
                    operations.put(op.symbol, op);
            }
        }

        public final String symbol;
        private final BinaryOperationApplication apply;

        private BinaryOperation(String symbol, BinaryOperationApplication apply) {
            this.symbol = symbol;
            this.apply = apply;
        }

        public AbstractedValue apply(AbstractedLogic logic, AbstractedValue value1, AbstractedValue value2) {
            return this.apply.apply(logic, value1, value2);
        }

        public static BinaryOperation get(String symbol) {
            return operations.getOrDefault(symbol, ABSTRACT);
        }
    }

    public static interface BinaryOperationApplication {
        AbstractedValue apply(AbstractedLogic logic, AbstractedValue value1, AbstractedValue value2);
    }

}
