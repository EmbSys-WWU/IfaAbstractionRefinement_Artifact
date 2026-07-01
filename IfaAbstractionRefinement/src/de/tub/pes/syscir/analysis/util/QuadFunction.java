package de.tub.pes.syscir.analysis.util;

/**
 * Functional interface for a function with four parameters.
 *
 * @author Jonas Becker-Kupczok
 *
 * @param <S> type of the first parameter
 * @param <T> type of the second parameter
 * @param <U> type of the third parameter
 * @param <V> type of the fourth parameter
 * @param <R> type of the result
 */
public interface QuadFunction<S, T, U, V, R> {

    /**
     * Applies this function.
     * 
     * @param s the first parameter
     * @param t the second parameter
     * @param u the third parameter
     * @param v the fourth parameter
     * @return the result
     */
    R apply(S s, T t, U u, V v);

}
