package de.tub.pes.syscir.analysis.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class WrappedSet<E> implements Set<E> {

    public static <E> WrappedSet<E> replaceInsertedElements(Set<E> original, Function<E, E> replacementFunction) {
        return new WrappedSet<>(original) {

            @Override
            public boolean add(E e) {
                return this.original.add(replacementFunction.apply(e));
            }

            @Override
            public boolean addAll(Collection<? extends E> c) {
                boolean result = false;
                for (E e : c) {
                    result |= add(e);
                }
                return result;
            }

        };
    }

    public static <E> WrappedSet<E> replaceRetrievedElements(Set<E> original, Function<E, E> replacementFunction) {
        return new WrappedSet<>(original) {

            @Override
            public Iterator<E> iterator() {
                return new Iterator<>() {

                    Iterator<E> internal = original.iterator();

                    public boolean hasNext() {
                        return this.internal.hasNext();
                    }

                    public E next() {
                        return replacementFunction.apply(this.internal.next());
                    }

                    public void remove() {
                        this.internal.remove();
                    }

                    public void forEachRemaining(Consumer<? super E> action) {
                        this.internal.forEachRemaining(e -> action.accept(replacementFunction.apply(e)));
                    }

                };
            }

            @SuppressWarnings("unchecked")
            @Override
            public Object[] toArray() {
                Object[] result = this.original.toArray();
                for (int i = 0; i < result.length; i++) {
                    result[i] = replacementFunction.apply((E) result[i]);
                }
                return result;
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> T[] toArray(T[] a) {
                T[] result = this.original.toArray(a);
                for (int i = 0; i < result.length; i++) {
                    result[i] = (T) replacementFunction.apply((E) result[i]);
                }
                return result;
            }

            @Override
            public Spliterator<E> spliterator() {
                return wrapSpliterator(this.original.spliterator());
            }

            private Spliterator<E> wrapSpliterator(Spliterator<E> internal) {
                return new Spliterator<>() {

                    Spliterator<E> internal = original.spliterator();

                    public boolean tryAdvance(Consumer<? super E> action) {
                        return this.internal.tryAdvance(e -> action.accept(replacementFunction.apply(e)));
                    }

                    public void forEachRemaining(Consumer<? super E> action) {
                        this.internal.forEachRemaining(e -> action.accept(replacementFunction.apply(e)));
                    }

                    public Spliterator<E> trySplit() {
                        return wrapSpliterator(this.internal.trySplit());
                    }

                    public long estimateSize() {
                        return this.internal.estimateSize();
                    }

                    public long getExactSizeIfKnown() {
                        return this.internal.getExactSizeIfKnown();
                    }

                    public int characteristics() {
                        return this.internal.characteristics();
                    }

                    public boolean hasCharacteristics(int characteristics) {
                        return this.internal.hasCharacteristics(characteristics);
                    }

                    public Comparator<? super E> getComparator() {
                        return this.internal.getComparator();
                    }
                };
            }

            @Override
            public <T> T[] toArray(IntFunction<T[]> generator) {
                return this.original.toArray(generator);
            }

            @Override
            public Stream<E> stream() {
                return this.original.stream();
            }

            @Override
            public Stream<E> parallelStream() {
                return this.original.parallelStream();
            }

        };
    }

    protected final Set<E> original;

    public WrappedSet(Set<E> original) {
        this.original = Objects.requireNonNull(original);
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        this.original.forEach(action);
    }

    @Override
    public int size() {
        return this.original.size();
    }

    @Override
    public boolean isEmpty() {
        return this.original.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.original.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return this.original.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.original.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.original.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return this.original.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return this.original.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.original.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return this.original.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.original.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.original.removeAll(c);
    }

    @Override
    public void clear() {
        this.original.clear();
    }

    @Override
    public boolean equals(Object o) {
        return this.original.equals(o);
    }

    @Override
    public int hashCode() {
        return this.original.hashCode();
    }

    @Override
    public Spliterator<E> spliterator() {
        return this.original.spliterator();
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return this.original.toArray(generator);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return this.original.removeIf(filter);
    }

    @Override
    public Stream<E> stream() {
        return this.original.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return this.original.parallelStream();
    }
}
