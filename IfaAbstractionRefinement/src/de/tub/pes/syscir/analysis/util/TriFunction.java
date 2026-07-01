package de.tub.pes.syscir.analysis.util;

/**
 * Functional interface for a function with three parameters.
 *
 * @author Jonas Becker-Kupczok
 *
 * @param <T> type of the first parameter
 * @param <U> type of the second parameter
 * @param <V> type of the third parameter
 * @param <R> type of the result
 */
public interface TriFunction<T, U, V, R> {

    /**
     * Applies this function.
     * 
     * @param t the first parameter
     * @param u the second parameter
     * @param v the third parameter
     * @return the result
     */
    R apply(T t, U u, V v);

}
