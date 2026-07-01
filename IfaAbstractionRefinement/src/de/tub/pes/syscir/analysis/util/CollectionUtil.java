package de.tub.pes.syscir.analysis.util;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Some utility for the use of collections.
 * 
 * @author Jonas Becker-Kupczok, Lukas Ernst
 *
 */
public class CollectionUtil {

    /**
     * Returns a set containing the null element.
     *
     * @param <T> a type
     * @return a set of that type containing only null
     */
    public static <T> Set<T> nullSet() {
        LinkedHashSet<T> result = new LinkedHashSet<>();
        result.add(null);
        return result;
    }

    /**
     * Puts the given value to the given index of the given list, overwriting any existing value at that
     * index and inserting null values to reach the index if necessary.
     *
     * @param <T> the type of elements of the list
     * @param list the list
     * @param index the index where to put the value
     * @param value the value
     */
    public static <T> void addOrSet(List<T> list, int index, T value) {
        while(list.size() < index) {
            list.add(null);
        }
        if (list.size() <= index) {
            list.add(value);
        } else {
            list.set(index, value);
        }
    }

    /**
     * Returns a new set containing all elements from the first that are not contained in the second.
     *
     * @param <T> the type of elements in the new set
     * @param a the first set
     * @param b the second set
     * @return a new set containing all elements from the first that are not contained in the second
     */
    public static <T> Set<T> setDiff(Set<? extends T> a, Set<? extends T> b) {
        Set<T> result = new LinkedHashSet<>(a);
        result.removeAll(b);
        return result;
    }

    /**
     * Returns a view on a set containing all elements contained in at least one of the parameters.
     *
     * @param <T> the type of elements in the view of the set
     * @param a the first set
     * @param b the second set
     * @return a view of a set containing all elements contained in the first or second set
     */
    public static <T> Set<T> union(Set<? extends T> a, Set<? extends T> b) {
        return new AbstractSet<>() {

            @Override
            public Iterator<T> iterator() {
                return concat(a, setDiff(b, a)).iterator();
            }

            @Override
            public int size() {
                int aSize = a.size();
                int bSize = b.size();
                if (aSize > bSize) {
                    return aSize + (int) b.stream().filter(Predicate.not(a::contains)).count();
                } else {
                    return bSize + (int) a.stream().filter(Predicate.not(b::contains)).count();
                }
            }

            @Override
            public boolean contains(Object o) {
                return a.contains(o) || b.contains(o);
            }

        };
    }

    /**
     * Returns a view on an iterable containing all elements from the first parameter followed by those
     * from the second parameter.
     *
     * @param <T> the type of elements in the iterable
     * @param a the first iterable
     * @param b the second iterable
     * @return a view on an iterable containing all elements contained in the first parameter followed
     *         by those in the second parameter
     */
    public static <T> Iterable<T> concat(Iterable<? extends T> a, Iterable<? extends T> b) {
        return new Iterable<>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<>() {

                    private Iterator<? extends T> aIt = a.iterator();
                    private Iterator<? extends T> bIt = b.iterator();

                    @Override
                    public boolean hasNext() {
                        return this.aIt.hasNext() || this.bIt.hasNext();
                    }

                    @Override
                    public T next() {
                        if (this.aIt.hasNext()) {
                            return this.aIt.next();
                        }
                        return this.bIt.next();
                    }

                };
            }
        };
    }

    /**
     * Returns a list representation of this collection.
     * 
     * If the collection already is a list, it is returned. Otherwise, a new list with the same content
     * is returned.
     * 
     * @param <T> a type
     * @param collection a collection of that type
     * @return a list of that type with the same contents
     */
    public static <T> List<T> asList(Collection<T> collection) {
        return collection instanceof List<T> l ? l : new ArrayList<>(collection);
    }

    /**
     * Creates a deeply unmodifiable list of lists.
     * 
     * Immutability is not extended deeper than one nested level, so if T is a collection type, elements
     * of type T may remain modifiable.
     *
     * @param <T> a type
     * @param list a list of lists of that type
     * @return an unmodifiable list of unmodifiable lists with the same elements
     */
    public static <T> List<List<T>> deeplyUnmodifiableList(List<? extends List<? extends T>> list) {
        return new AbstractList<>() {

            @Override
            public List<T> get(int index) {
                return Collections.unmodifiableList(list.get(index));
            }

            @Override
            public int size() {
                return list.size();
            }

            @Override
            public boolean equals(Object o) {
                return list.equals(o);
            }

            @Override
            public int hashCode() {
                return list.hashCode();
            }

        };
    }

    /**
     * Creates a deep copy of a list of lists.
     * 
     * Only lists nested one level are cloned, so if T is a collection type, elements of type T may
     * remain uncloned.
     * 
     * @param <T> a type
     * @param list a list of lists of that type
     * @return a list of copies of theses lists
     */
    public static <T> List<List<T>> deepCopy(List<? extends List<? extends T>> list) {
        List<List<T>> result = new ArrayList<>(list.size());
        for (List<? extends T> inner : list) {
            result.add(new ArrayList<>(inner));
        }
        return result;
    }

    /**
     * Returns the element at the given index in the given iterable.
     * 
     * If the iterable doesn't contain that many elements, a {@link NoSuchElementException} is thrown.
     * 
     * @param <T> a type
     * @param iterable an iterable of that type
     * @param index an index
     * @return the element at that index in the iterable
     * @throws NoSuchElementException if the iterable doesn't have that many elements
     */
    public static <T> T getElement(Iterable<T> iterable, int index) throws NoSuchElementException {
        Iterator<T> it = iterable.iterator();
        for (int i = 0; i < index; i++) {
            it.next();
        }
        return it.next();
    }

    /**
     * Returns the hashCode that the specified map would have after removing all
     * entries that do not satisfy the specified consideration condition.
     */
    public static <K, V> int partialMapHashCode(Map<K, V> map, MapEntryFilter<K, V> considerationCondition) {
        int hash = 0;
        for (Entry<K, V> entry : map.entrySet()) {
            if (considerationCondition.test(entry.getKey(), entry.getValue()))
                hash += entry.hashCode();
        }
        return hash;
    }

    private static final Object UNMAPPED_INDICATOR = new Object();

    /**
     * Returns whether the specified maps would be equal after removing all entries
     * that do not satisfy the respective consideration condition.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> boolean partialMapEquals(Map<K, V> map1, MapEntryFilter<K, V> considerationCondition1,
            Map<K, V> map2, MapEntryFilter<K, V> considerationCondition2) {
        int consideredSize2 = 0;
        for (Entry<K, V> entry2 : map2.entrySet()) {
            if (considerationCondition2.test(entry2.getKey(), entry2.getValue()))
                consideredSize2++;
        }
        int consideredSize1 = 0;
        for (Entry<K, V> entry1 : map1.entrySet()) {
            K key1 = entry1.getKey();
            V value1 = entry1.getValue();
            if (!considerationCondition1.test(key1, value1))
                continue;
            consideredSize1++;
            Object value2 = ((Map<Object, Object>) map2).getOrDefault(key1, UNMAPPED_INDICATOR);
            if (value2 == UNMAPPED_INDICATOR || !considerationCondition2.test(key1, (V) value2) || !Objects.equals(value1, value2))
                return false;
        }
        return consideredSize1 == consideredSize2;
    }

    public static interface MapEntryFilter<K, V> {

        boolean test(K key, V value);
    }

}
