/*
 * MODIFIED. Based on the LinkedHashMap implementation from Apache Harmony. Original licensing information:
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
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

/**
 * Variant of a {@link LinkedHashMap} where the hash function and equality can be specified in the
 * constructor.
 *
 * @author Jonas Becker-Kupczok
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @see LinkedHashMap
 * @see GeneralHashMap
 */
public class LinkedGeneralHashMap<K, V> extends GeneralHashMap<K, V> {

    private class AbstractMapIterator {

        int expectedModCount;
        LinkedEntry futureEntry;
        LinkedEntry currentEntry;

        AbstractMapIterator() {
            this.expectedModCount = LinkedGeneralHashMap.this.modCount;
            this.futureEntry = LinkedGeneralHashMap.this.head;
        }

        public boolean hasNext() {
            return (this.futureEntry != null);
        }

        final void checkConcurrentMod() throws ConcurrentModificationException {
            if (this.expectedModCount != LinkedGeneralHashMap.this.modCount) {
                throw new ConcurrentModificationException();
            }
        }

        final void makeNext() {
            checkConcurrentMod();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            this.currentEntry = this.futureEntry;
            this.futureEntry = this.futureEntry.chainForward;
        }

        public void remove() {
            checkConcurrentMod();
            if (this.currentEntry == null) {
                throw new IllegalStateException();
            }
            LinkedGeneralHashMap.this.removeEntry(this.currentEntry);
            LinkedEntry lhme = this.currentEntry;
            LinkedEntry p = lhme.chainBackward;
            LinkedEntry n = lhme.chainForward;

            if (p != null) {
                p.chainForward = n;
                if (n != null) {
                    n.chainBackward = p;
                } else {
                    LinkedGeneralHashMap.this.tail = p;
                }
            } else {
                LinkedGeneralHashMap.this.head = n;
                if (n != null) {
                    n.chainBackward = null;
                } else {
                    LinkedGeneralHashMap.this.tail = null;
                }
            }
            this.currentEntry = null;
            this.expectedModCount++;
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

    final class LinkedEntrySet extends EntrySet {

        public LinkedEntrySet() {

        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }
    }

    class LinkedEntry extends GeneralHashMap<K, V>.Entry {

        LinkedEntry chainForward, chainBackward;

        LinkedEntry(K theKey, V theValue) {
            super(theKey, theValue);
            this.chainForward = null;
            this.chainBackward = null;
        }

        LinkedEntry(K theKey, int hash) {
            super(theKey, hash);
            this.chainForward = null;
            this.chainBackward = null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object clone() {
            LinkedEntry entry = (LinkedEntry) super.clone();
            entry.chainBackward = this.chainBackward;
            entry.chainForward = this.chainForward;
            LinkedEntry lnext = (LinkedEntry) entry.next;
            if (lnext != null) {
                entry.next = (LinkedEntry) lnext.clone();
            }
            return entry;
        }
    }

    private final boolean accessOrder;
    private transient LinkedEntry head, tail;

    /**
     * Constructs a new insertion-ordered {@code GeneralHashMap} instance with the specified hasher,
     * equality, initial capacity and load factor.
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
    public LinkedGeneralHashMap(ToIntFunction<? super K> hasher, BiPredicate<? super K, ? super K> equality, int initialCapacity, float loadFactor) {
        super(hasher, equality, initialCapacity, loadFactor);
        this.head = null;
        this.tail = null;
        this.accessOrder = false;
    }

    /**
     * Constructs a new insertion-ordered {@code GeneralHashMap} instance with the specified hasher,
     * equality and initial capacity.
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
    public LinkedGeneralHashMap(ToIntFunction<? super K> hasher, BiPredicate<? super K, ? super K> equality, int initialCapacity) {
        super(hasher, equality, initialCapacity);
        this.head = null;
        this.tail = null;
        this.accessOrder = false;
    }

    /**
     * Constructs a new insertion-ordered {@code GeneralHashMap} instance with the specified hasher and
     * equality.
     *
     * @param hasher
     *            the function to hash keys with.
     * @param equality
     *            the predicate to compare keys with.
     * @throws NullPointerException
     *             when the hasher or equality is null.
     */
    public LinkedGeneralHashMap(ToIntFunction<? super K> hasher, BiPredicate<? super K, ? super K> equality) {
        super(hasher, equality);
        this.head = null;
        this.tail = null;
        this.accessOrder = false;
    }

    /**
     * Constructs a new insertion-ordered {@code LinkedGeneralHashMap} instance with the specified
     * hasher and equality and containing the mappings from the specified map.
     *
     * @param hasher
     *            the function to hash keys with.
     * @param equality
     *            the predicate to compare keys with.
     * @throws NullPointerException
     *             when the hasher or equality or the given map is null.
     */
    public LinkedGeneralHashMap(ToIntFunction<? super K> hasher, BiPredicate<? super K, ? super K> equality, Map<? extends K, ? extends V> m) {
        super(hasher, equality);
        this.accessOrder = false;
        this.head = null;
        this.tail = null;
        putAll(m);
    }

    /**
     * Constructs a new {@code LinkedGeneralHashMap} instance with the specified hayher, equality,
     * initial capacity, load factor and ordering mode.
     *
     * @param hasher
     *            the function to hash keys with.
     * @param equality
     *            the predicate to compare keys with.
     * @param initialCapacity
     *            the initial capacity of this hash map.
     * @param loadFactor
     *            the load factor.
     * @param accessOrder
     *            the ordering mode - <tt>true</tt> for access-order, <tt>false</tt> for
     *            insertion-order
     * @throws IllegalArgumentException
     *             when the capacity is less than zero or the load factor is less
     *             or equal to zero.
     * @throws NullPointerException
     *             when the hasher or equality is null.
     */
    public LinkedGeneralHashMap(ToIntFunction<? super K> hasher, BiPredicate<? super K, ? super K> equality, int initialCapacity, float loadFactor, boolean accessOrder) {
        super(hasher, equality, initialCapacity, loadFactor);
        this.head = null;
        this.tail = null;
        this.accessOrder = accessOrder;
    }

    /**
     * Create a new element array
     *
     * @param s
     * @return Reference to the element array
     */
    @Override
    @SuppressWarnings("unchecked")
    LinkedEntry[] newElementArray(int s) {
        return new LinkedGeneralHashMap.LinkedEntry[s];
    }

    @Override
    public boolean containsValue(Object value) {
        LinkedEntry entry = this.head;
        if (null == value) {
            while (null != entry) {
                if (null == entry.value) {
                    return true;
                }
                entry = entry.chainForward;
            }
        } else {
            while (null != entry) {
                if (value.equals(entry.value)) {
                    return true;
                }
                entry = entry.chainForward;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        LinkedEntry m;
        if ((m = (LinkedEntry) getEntry(key)) == null) {
            return null;
        }
        if (this.accessOrder && this.tail != m) {
            LinkedEntry p = m.chainBackward;
            LinkedEntry n = m.chainForward;
            n.chainBackward = p;
            if (p != null) {
                p.chainForward = n;
            } else {
                this.head = n;
            }
            m.chainForward = null;
            m.chainBackward = this.tail;
            this.tail.chainForward = m;
            this.tail = m;
        }
        return m.value;
    }

    /*
     * @param key @param index @return Entry
     */
    @Override
    Entry createEntry(K key, int index, V value) {
        LinkedEntry m = new LinkedEntry(key, value);
        m.next = this.elementData[index];
        this.elementData[index] = m;
        linkEntry(m);
        return m;
    }

    @Override
    Entry createHashedEntry(K key, int index, int hash) {
        LinkedEntry m = new LinkedEntry(key, hash);
        m.next = this.elementData[index];
        this.elementData[index] = m;
        linkEntry(m);
        return m;
    }

    /**
     * Maps the specified key to the specified value.
     *
     * @param key
     *            the key.
     * @param value
     *            the value.
     * @return the value of any previous mapping with the specified key or {@code null} if there was no
     *         such mapping.
     */
    @Override
    public V put(K key, V value) {
        V result = putImpl(key, value);

        if (removeEldestEntry(this.head)) {
            remove(this.head.key);
        }

        return result;
    }

    @Override
    V putImpl(K key, V value) {
        LinkedEntry m;
        if (this.elementCount == 0) {
            this.head = this.tail = null;
        }
        int hash = computeHashCode(key);
        int index = (hash & 0x7FFFFFFF) % this.elementData.length;
        m = (LinkedEntry) findEntry(key, index, hash);
        if (m == null) {
            this.modCount++;
            if (++this.elementCount > this.threshold) {
                rehash();
                index = (hash & 0x7FFFFFFF) % this.elementData.length;
            }
            m = (LinkedEntry) createHashedEntry(key, index, hash);
        } else {
            linkEntry(m);
        }

        V result = m.value;
        m.value = value;
        return result;
    }

    /*
     * @param m
     */
    void linkEntry(LinkedEntry m) {
        if (this.tail == m) {
            return;
        }

        if (this.head == null) {
            // Check if the map is empty
            this.head = this.tail = m;
            return;
        }

        // we need to link the new entry into either the head or tail
        // of the chain depending on if the LinkedHashMap is accessOrder or not
        LinkedEntry p = m.chainBackward;
        LinkedEntry n = m.chainForward;
        if (p == null) {
            if (n != null) {
                // The entry must be the head but not the tail
                if (this.accessOrder) {
                    this.head = n;
                    n.chainBackward = null;
                    m.chainBackward = this.tail;
                    m.chainForward = null;
                    this.tail.chainForward = m;
                    this.tail = m;
                }
            } else {
                // This is a new entry
                m.chainBackward = this.tail;
                m.chainForward = null;
                this.tail.chainForward = m;
                this.tail = m;
            }
            return;
        }

        if (n == null) {
            // The entry must be the tail so we can't get here
            return;
        }

        // The entry is neither the head nor tail
        if (this.accessOrder) {
            p.chainForward = n;
            n.chainBackward = p;
            m.chainForward = null;
            m.chainBackward = this.tail;
            this.tail.chainForward = m;
            this.tail = m;
        }
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (this.entrySet == null) {
            this.entrySet = new LinkedEntrySet();
        }
        return this.entrySet;
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
                    return LinkedGeneralHashMap.this.size();
                }

                @Override
                public void clear() {
                    LinkedGeneralHashMap.this.clear();
                }

                @Override
                public boolean remove(Object key) {
                    if (containsKey(key)) {
                        LinkedGeneralHashMap.this.remove(key);
                        return true;
                    }
                    return false;
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
    public Collection<V> values() {
        if (this.values == null) {
            this.values = new AbstractCollection<V>() {

                @Override
                public boolean contains(Object object) {
                    return containsValue(object);
                }

                @Override
                public int size() {
                    return LinkedGeneralHashMap.this.size();
                }

                @Override
                public void clear() {
                    LinkedGeneralHashMap.this.clear();
                }

                @Override
                public Iterator<V> iterator() {
                    return new ValueIterator();
                }
            };
        }
        return this.values;
    }

    @Override
    public V remove(Object key) {
        LinkedEntry m = (LinkedEntry) removeEntry(key);
        if (m == null) {
            return null;
        }
        LinkedEntry p = m.chainBackward;
        LinkedEntry n = m.chainForward;
        if (p != null) {
            p.chainForward = n;
        } else {
            this.head = n;
        }
        if (n != null) {
            n.chainBackward = p;
        } else {
            this.tail = p;
        }
        return m.value;
    }

    /**
     * This method is queried from the put and putAll methods to check if the eldest member of the map
     * should be deleted before adding the new member. If this map was created with accessOrder = true,
     * then the result of removeEldestEntry is assumed to be false.
     *
     * @param eldest
     *            the entry to check if it should be removed.
     * @return {@code true} if the eldest member should be removed.
     */
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return false;
    }

    @Override
    public void clear() {
        super.clear();
        this.head = this.tail = null;
    }

}
