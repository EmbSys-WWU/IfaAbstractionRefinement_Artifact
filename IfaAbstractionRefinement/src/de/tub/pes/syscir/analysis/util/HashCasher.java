package de.tub.pes.syscir.analysis.util;

import java.util.Objects;
import java.util.function.ToIntFunction;

public class HashCasher<T> extends HashCachingLockableObject {

    private ToIntFunction<T> hashFunction;
    private T instance;

    public HashCasher(ToIntFunction<T> hashFunction, T instance) {
        this.hashFunction = Objects.requireNonNull(hashFunction);
        this.instance = Objects.requireNonNull(instance);
    }

    public HashCasher(HashCasher<T> copyOf, T instance) {
        super(copyOf);

        this.hashFunction = copyOf.hashFunction;
        this.instance = Objects.requireNonNull(instance);
    }

    @Override
    protected int hashCodeInternal() {
        return this.hashFunction.applyAsInt(this.instance);
    }

    @Override
    public void resetHashCode() {
        super.resetHashCode();
    }

    @Override
    public boolean lock() {
        return super.lock();
    }

    @Override
    public HashCasher<T> unlockedClone() {
        throw new UnsupportedOperationException();
    }

}
