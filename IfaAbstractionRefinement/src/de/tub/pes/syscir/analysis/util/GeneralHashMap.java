/*
 * MODIFIED. Based on the HashMap implementation from Apache Harmony. Original licensing information:
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.tub.pes.syscir.analysis.util;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

/**
 * Variant of a {@link HashMap} where the hash function and equality can be specified in the
 * constructor.
 * 
 * The equality predicate is always called with the key contained in the map as the first parameter
 * and any object passed from outside (e.g. in {@link #containsKey(Object)} or
 * {@link #put(Object, Object)}) as the second parameter. Thus, using asymmetric "equalities" is
 * generally legal, but may lead to behavior not expected from maps.
 *
 * @author Jonas Becker-Kupczok
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @see HashMap
 */
public class GeneralHashMap<K, V> extends AbstractMap<K, V> implements Cloneable {

    public static final ToIntFunction<Object> DEFAULT_HASHER = Objects::hashCode;
    public static final BiPredicate<Object, Object> DEFAULT_EQUALITY = Objects::equals;

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * Creates a wrapper around the given original that returns 0 if the input is null and delegates it
     * to the original otherwise.
     *
     * @param original
     *            the hasher to be wrapped
     * @return null resistant wrapper hasher
     */
    public static <T> ToIntFunction<T> createNullResistantHasher(ToIntFunction<T> original) {
        return x -> x == null ? 0 : original.applyAsInt(x);
    }

    /**
     * Creates a wrapper around the given original that returns 0 if the input is not of the given type
     * and delegates it to the original otherwise.
     * <p>
     * If the input is null, it is delegated.
     *
     * @param original
     *            the hasher to be wrapped
     * @param type
     *            the type of which all non-delegated input must be
     * @return type resistant wrapper hasher
     */
    @SuppressWarnings("unchecked")
    public static <T> ToIntFunction<Object> createTypeResistantHasher(ToIntFunction<? super T> original, Class<T> type) {
        return x -> {
            if (x != null && !type.isInstance(x)) {
                return original.applyAsInt((T) x);
            }
            return 0;
        };
    }

    /**
     * Creates a wrapper around the given original that returns 0 if the input is null or not of the
     * given type and delegates it to the original otherwise.
     *
     * @param original
     *            the hasher to be wrapped
     * @param type
     *            the type of which all non-delegated input must be
     * @return type and null resistant wrapper hasher
     */
    @SuppressWarnings("unchecked")
    public static <T> ToIntFunction<Object> createResistantHasher(ToIntFunction<? super T> original, Class<T> type) {
        return x -> {
            if (x == null) {
                return 0;
            }
            return type.isInstance(x) ? original.applyAsInt((T) x) : 0;
        };
    }

    /**
     * Calculates the capacity of storage required for storing given number of elements
     *
     * @param x
     *            number of elements
     * @return storage size
     */
    private static final int calculateCapacity(int x) {
        if (x >= MAXIMUM_CAPACITY) {
            return MAXIMUM_CAPACITY;
        }
        if (x == 0) {
            return 16;
        }
        x = x - 1;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return x + 1;
    }

    class Entry implements Map.Entry<K, V>, Cloneable {

        final K key;
        V value;

        final int origKeyHash;
        Entry next;

        Entry(K theKey, int hash) {
            this.origKeyHash = hash;
            this.key = theKey;
            this.value = null;
            this.next = null;
        }

        Entry(K theKey, V theValue) {
            this.origKeyHash = (theKey == null ? 0 : GeneralHashMap.this.computeHashCode(theKey));
            this.key = theKey;
            this.value = theValue;
            this.next = null;
        }

        @Override
        public final K getKey() {
            return this.key;
        }

        @Override
        public final V getValue() {
            return this.value;
        }

        @Override
        public final String toString() {
            return this.key + "=" + this.value;
        }

        @Override
        public final int hashCode() {
            return GeneralHashMap.this.computeHashCode(this.key) ^ Objects.hashCode(this.value);
        }

        @Override
        public final V setValue(V newValue) {
            V oldValue = this.value;
            this.value = newValue;
            return oldValue;
        }

        @Override
        public final boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                if (GeneralHashMap.this.areEqualKeys(this.key, e.getKey()) && GeneralHashMap.this.areEqualValues(this.value, e.getValue())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object clone() {
            Entry entry;
            try {
                entry = (Entry) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
            if (this.next != null) {
                entry.next = (Entry) this.next.clone();
            }
            return entry;
        }
    }

    private class AbstractMapIterator {

        private int position = 0;
        int expectedModCount;
        Entry futureEntry;
        Entry currentEntry;
        Entry prevEntry;

        AbstractMapIterator() {
            this.expectedModCount = GeneralHashMap.this.modCount;
            this.futureEntry = null;
        }

        public boolean hasNext() {
            if (this.futureEntry != null) {
                return true;
            }
            while (this.position < GeneralHashMap.this.elementData.length) {
                if (GeneralHashMap.this.elementData[this.position] == null) {
                    this.position++;
                } else {
                    return true;
                }
            }
            return false;
        }

        final void checkConcurrentMod() throws ConcurrentModificationException {
            if (this.expectedModCount != GeneralHashMap.this.modCount) {
                throw new ConcurrentModificationException();
            }
        }

        final void makeNext() {
            checkConcurrentMod();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (this.futureEntry == null) {
                this.currentEntry = GeneralHashMap.this.elementData[this.position++];
                this.futureEntry = this.currentEntry.next;
                this.prevEntry = null;
            } else {
                if (this.currentEntry != null) {
                    this.prevEntry = this.currentEntry;
                }
                this.currentEntry = this.futureEntry;
                this.futureEntry = this.futureEntry.next;
            }
        }

        public final void remove() {
            checkConcurrentMod();
            if (this.currentEntry == null) {
                throw new IllegalStateException();
            }
            if (this.prevEntry == null) {
                int index = this.currentEntry.origKeyHash & (GeneralHashMap.this.elementData.length - 1);
                GeneralHashMap.this.elementData[index] = GeneralHashMap.this.elementData[index].next;
            } else {
                this.prevEntry.next = this.currentEntry.next;
            }
            this.currentEntry = null;
            this.expectedModCount++;
            GeneralHashMap.this.modCount++;
            GeneralHashMap.this.elementCount--;

        }
    }

    private class EntryIterator extends AbstractMapIterator implements Iterator<Map.Entry<K, V>> {

        EntryIterator() {

        }

        @Override
        public Map.Entry<K, V> next() {
            makeNext();
            return this.currentEntry;
        }
    }

    private class KeyIterator extends AbstractMapIterator implements Iterator<K> {

        KeyIterator() {

        }

        @Override
        public K next() {
            makeNext();
            return this.currentEntry.key;
        }
    }

    private class ValueIterator extends AbstractMapIterator implements Iterator<V> {

        ValueIterator() {

        }

        @Override
        public V next() {
            makeNext();
            return this.currentEntry.value;
        }
    }

    class EntrySet extends AbstractSet<Map.Entry<K, V>> {

        EntrySet() {

        }

        @Override
        public int size() {
            return GeneralHashMap.this.elementCount;
        }

        @Override
        public void clear() {
            GeneralHashMap.this.clear();
        }

        @Override
        public boolean remove(Object object) {
            if (object instanceof Map.Entry) {
                Map.Entry<?, ?> oEntry = (Map.Entry<?, ?>) object;
                Entry entry = GeneralHashMap.this.getEntry(oEntry.getKey());
                if (valuesEq(entry, oEntry)) {
                    GeneralHashMap.this.removeEntry(entry);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean contains(Object object) {
            if (object instanceof Map.Entry) {
                Map.Entry<?, ?> oEntry = (Map.Entry<?, ?>) object;
                Entry entry = GeneralHashMap.this.getEntry(oEntry.getKey());
                return valuesEq(entry, oEntry);
            }
            return false;
        }

        private boolean valuesEq(Entry entry, Map.Entry<?, ?> oEntry) {
            return (entry != null) && ((entry.value == null) ? (oEntry.getValue() == null) : (areEqualValues(entry.value, oEntry.getValue())));
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }
    }

    /**
     * The hash function used by this GeneralHashMap.
     *
     */
    private final ToIntFunction<? super K> hasher;

    /**
     * The equality used to compare keys.
     *
     */
    private final BiPredicate<? super K, ? super K> equality;

    /*
     * Actual count of entries
     */
    int elementCount;

    /*
     * The internal data structure to hold Entries
     */
    Entry[] elementData;

    /*
     * modification count, to keep track of structural modifications between the HashMap and the
     * iterator
     */
    int modCount = 0;

    /*
     * maximum ratio of (stored elements)/(storage size) which does not lead to rehash
     */
    private final float loadFactor;

    /*
     * maximum number of elements that can be put in this map before having to rehash
     */
    int threshold;

    // cached views on the map
    Set<Map.Entry<K, V>> entrySet;
    Set<K> keySet;
    Collection<V> values;

    /**
     * Constructs a new {@code GeneralHashMap} instance with the specified hasher, equality, initial
     * capacity and load factor.
     *
     * @param hasher
     *            the function to hash keys with.
     * @param equality
     *            the predicate to compare keys with.
     * @param initialCapacity
     *            the initial capacity of this hash map.
     * @param loadFactor
     *            the load factor.
     * @throws IllegalArgumentException
     *             when the capacity is less than zero or the load factor is less
     *             or equal to zero.
     * @throws NullPointerException
     *             when the hasher or equality is null.
     */
    public GeneralHashMap(ToIntFunction<? super K> hasher, BiPredicate<? super K, ? super K> equality, int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }

        initialCapacity = calculateCapacity(initialCapacity);
        this.elementData = newElementArray(initialCapacity);

        this.loadFactor = loadFactor;
        this.elementCount = 0;
        computeThreshold();

        this.hasher = Objects.requireNonNull(hasher);
        this.equality = Objects.requireNonNull(equality);
    }

    /**
     * Constructs a new {@code GeneralHashMap} instance with the specified hasher, equality and initial
     * capacity.
     *
     * @param hasher
     *            the function to hash keys with.
     * @param equality
     *            the predicate to compare keys with.
     * @param initialCapacity
     *            the initial capacity of this hash map.
     * @throws IllegalArgumentException
     *             when the capacity is less than zero.
     * @throws NullPointerException
     *             when the hasher or equality is null.
     */
    public GeneralHashMap(ToIntFunction<? super K> hasher, BiPredicate<? super K, ? super K> equality, int initialCapacity) {
        this(hasher, equality, initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new {@code GeneralHashMap} instance with the specified hasher and equality.
     *
     * @param hasher
     *            the function to hash keys with.
     * @param equality
     *            the predicate to compare keys with.
     * @throws NullPointerException
     *             when the hasher or equality is null.
     */
    public GeneralHashMap(ToIntFunction<? super K> hasher, BiPredicate<? super K, ? super K> equality) {
        this(hasher, equality, DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new {@code GeneralHashMap} instance with the specified hash and equality and
     * containing the mappings from the specified map.
     *
     * @param hasher
     *            the function to hash keys with.
     * @param equality
     *            the predicate to compare keys with.
     * @throws NullPointerException
     *             when the hasher or equality is null or the given map is null.
     */
    public GeneralHashMap(ToIntFunction<? super K> hasher, BiPredicate<? super K, ? super K> equality, Map<? extends K, ? extends V> copyOf) {
        this(hasher, equality, DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
        putAllImpl(copyOf);
    }

    /**
     * Create a new element array
     *
     * @param s
     * @return Reference to the element array
     */
    @SuppressWarnings("unchecked")
    Entry[] newElementArray(int s) {
        return new GeneralHashMap.Entry[s];
    }

    @Override
    public void clear() {
        if (this.elementCount > 0) {
            this.elementCount = 0;
            Arrays.fill(this.elementData, null);
            this.modCount++;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            GeneralHashMap<K, V> map = (GeneralHashMap<K, V>) super.clone();
            map.elementCount = 0;
            map.elementData = newElementArray(this.elementData.length);
            map.putAll(this);

            return map;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Computes the threshold for rehashing
     */
    private void computeThreshold() {
        this.threshold = (int) (this.elementData.length * this.loadFactor);
    }

    @Override
    public boolean containsKey(Object key) {
        Entry m = getEntry(key);
        return m != null;
    }

    @Override
    public boolean containsValue(Object value) {
        if (value != null) {
            for (int i = 0; i < this.elementData.length; i++) {
                Entry entry = this.elementData[i];
                while (entry != null) {
                    if (areEqualValues(value, entry.value)) {
                        return true;
                    }
                    entry = entry.next;
                }
            }
        } else {
            for (int i = 0; i < this.elementData.length; i++) {
                Entry entry = this.elementData[i];
                while (entry != null) {
                    if (entry.value == null) {
                        return true;
                    }
                    entry = entry.next;
                }
            }
        }
        return false;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (this.entrySet == null) {
            this.entrySet = new EntrySet();
        }
        return this.entrySet;
    }

    @Override
    public V get(Object key) {
        Entry m = getEntry(key);
        if (m != null) {
            return m.value;
        }
        return null;
    }

    final Entry getEntry(Object key) {
        Entry m;
        int hash = computeHashCode(key);
        int index = hash & (this.elementData.length - 1);
        m = findEntry(key, index, hash);
        return m;
    }

    final Entry findEntry(Object key, int index, int keyHash) {
        Entry m = this.elementData[index];
        while (m != null && (m.origKeyHash != keyHash || !areEqualKeys(m.key, key))) {
            m = m.next;
        }
        return m;
    }

    @Override
    public boolean isEmpty() {
        return this.elementCount == 0;
    }

    @Override
    public Set<K> keySet() {
        if (this.keySet == null) {
            this.keySet = new AbstractSet<K>() {

                @Override
                public boolean contains(Object object) {
                    return containsKey(object);
                }

                @Override
                public int size() {
                    return GeneralHashMap.this.size();
                }

                @Override
                public void clear() {
                    GeneralHashMap.this.clear();
                }

                @Override
                public boolean remove(Object key) {
                    Entry entry = GeneralHashMap.this.removeEntry(key);
                    return entry != null;
                }

                @Override
                public Iterator<K> iterator() {
                    return new KeyIterator();
                }
            };
        }
        return this.keySet;
    }

    @Override
    public V put(K key, V value) {
        return putImpl(key, value);
    }

    V putImpl(K key, V value) {
        Entry entry;
        int hash = computeHashCode(key);
        int index = hash & (this.elementData.length - 1);
        entry = findEntry(key, index, hash);
        if (entry == null) {
            this.modCount++;
            entry = createHashedEntry(key, index, hash);
            if (++this.elementCount > this.threshold) {
                rehash();
            }
        }

        V result = entry.value;
        entry.value = value;
        return result;
    }

    Entry createEntry(K key, int index, V value) {
        Entry entry = new Entry(key, value);
        entry.next = this.elementData[index];
        this.elementData[index] = entry;
        return entry;
    }

    Entry createHashedEntry(K key, int index, int hash) {
        Entry entry = new Entry(key, hash);
        entry.next = this.elementData[index];
        this.elementData[index] = entry;
        return entry;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        if (!map.isEmpty()) {
            putAllImpl(map);
        }
    }

    private void putAllImpl(Map<? extends K, ? extends V> map) {
        int capacity = this.elementCount + map.size();
        if (capacity > this.threshold) {
            rehash(capacity);
        }
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            putImpl(entry.getKey(), entry.getValue());
        }
    }

    void rehash(int capacity) {
        int length = calculateCapacity((capacity == 0 ? 1 : capacity << 1));

        Entry[] newData = newElementArray(length);
        for (int i = 0; i < this.elementData.length; i++) {
            Entry entry = this.elementData[i];
            this.elementData[i] = null;
            while (entry != null) {
                int index = entry.origKeyHash & (length - 1);
                Entry next = entry.next;
                entry.next = newData[index];
                newData[index] = entry;
                entry = next;
            }
        }
        this.elementData = newData;
        computeThreshold();
    }

    void rehash() {
        rehash(this.elementData.length);
    }

    @Override
    public V remove(Object key) {
        Entry entry = removeEntry(key);
        if (entry != null) {
            return entry.value;
        }
        return null;
    }

    /*
     * Remove the given entry from the hashmap. Assumes that the entry is in the map.
     */
    final void removeEntry(Entry entry) {
        int index = entry.origKeyHash & (this.elementData.length - 1);
        Entry m = this.elementData[index];
        if (m == entry) {
            this.elementData[index] = entry.next;
        } else {
            while (m.next != entry) {
                m = m.next;
            }
            m.next = entry.next;

        }
        this.modCount++;
        this.elementCount--;
    }

    final Entry removeEntry(Object key) {
        int index = 0;
        Entry entry;
        Entry last = null;
        if (key != null) {
            int hash = computeHashCode(key);
            index = hash & (this.elementData.length - 1);
            entry = this.elementData[index];
            while (entry != null && !(entry.origKeyHash == hash && areEqualKeys(entry.key, key))) {
                last = entry;
                entry = entry.next;
            }
        } else {
            entry = this.elementData[0];
            while (entry != null && entry.key != null) {
                last = entry;
                entry = entry.next;
            }
        }
        if (entry == null) {
            return null;
        }
        if (last == null) {
            this.elementData[index] = entry.next;
        } else {
            last.next = entry.next;
        }
        this.modCount++;
        this.elementCount--;
        return entry;
    }

    @Override
    public int size() {
        return this.elementCount;
    }

    @Override
    public Collection<V> values() {
        if (this.values == null) {
            this.values = new AbstractCollection<V>() {

                @Override
                public boolean contains(Object object) {
                    return containsValue(object);
                }

                @Override
                public int size() {
                    return GeneralHashMap.this.size();
                }

                @Override
                public void clear() {
                    GeneralHashMap.this.clear();
                }

                @Override
                public Iterator<V> iterator() {
                    return new ValueIterator();
                }
            };
        }
        return this.values;
    }

    @SuppressWarnings("unchecked")
    int computeHashCode(Object key) {
        try {
            int h = this.hasher.applyAsInt((K) key);
            return h ^ (h >>> 16);
        } catch (ClassCastException e) {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean areEqualKeys(Object key1, Object key2) {
        try {
            return this.equality.test((K) key1, (K) key2);
        } catch (ClassCastException e) {
            return false;
        }
    }

    private boolean areEqualValues(Object value1, Object value2) {
        return Objects.equals(value1, value2);
    }

}
