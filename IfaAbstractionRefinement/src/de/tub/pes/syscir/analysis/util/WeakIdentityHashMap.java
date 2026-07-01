/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * Modification made from the original by Jonas Becker-Kupczok: Specify that the implementation is
 * synchronized.
 */

package de.tub.pes.syscir.analysis.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements a combination of WeakHashMap and IdentityHashMap. Useful for caches that need to key
 * off of a == comparison instead of a .equals.
 *
 * <b> This class is not a general-purpose Map implementation! While this class implements the Map
 * interface, it intentionally violates Map's general contract, which mandates the use of the equals
 * method when comparing objects. This class is designed for use only in the rare cases wherein
 * reference-equality semantics are required.
 *
 * This implementation is synchronized.</b>
 */
public class WeakIdentityHashMap<K, V> implements Map<K, V> {

    private final ReferenceQueue<K> queue = new ReferenceQueue<>();
    private Map<IdentityWeakReference, V> backingStore = new ConcurrentHashMap<>();

    public WeakIdentityHashMap() {}

    @Override
    public void clear() {
        this.backingStore.clear();
        reap();
    }

    @Override
    public boolean containsKey(Object key) {
        reap();
        return this.backingStore.containsKey(new IdentityWeakReference(key));
    }

    @Override
    public boolean containsValue(Object value) {
        reap();
        return this.backingStore.containsValue(value);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        reap();
        Set<Map.Entry<K, V>> ret = new HashSet<>();
        for (Map.Entry<IdentityWeakReference, V> ref : this.backingStore.entrySet()) {
            final K key = ref.getKey().get();
            final V value = ref.getValue();
            Map.Entry<K, V> entry = new Map.Entry<K, V>() {

                @Override
                public K getKey() {
                    return key;
                }

                @Override
                public V getValue() {
                    return value;
                }

                @Override
                public V setValue(V value) {
                    throw new UnsupportedOperationException();
                }
            };
            ret.add(entry);
        }
        return Collections.unmodifiableSet(ret);
    }

    @Override
    public Set<K> keySet() {
        reap();
        Set<K> ret = new HashSet<>();
        for (IdentityWeakReference ref : this.backingStore.keySet()) {
            ret.add(ref.get());
        }
        return Collections.unmodifiableSet(ret);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WeakIdentityHashMap)) {
            return false;
        }
        return this.backingStore.equals(((WeakIdentityHashMap<?, ?>) o).backingStore);
    }

    @Override
    public V get(Object key) {
        reap();
        return this.backingStore.get(new IdentityWeakReference(key));
    }

    @Override
    public V put(K key, V value) {
        reap();
        return this.backingStore.put(new IdentityWeakReference(key), value);
    }

    @Override
    public int hashCode() {
        reap();
        return this.backingStore.hashCode();
    }

    @Override
    public boolean isEmpty() {
        reap();
        return this.backingStore.isEmpty();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        reap();
        return this.backingStore.remove(new IdentityWeakReference(key));
    }

    @Override
    public int size() {
        reap();
        return this.backingStore.size();
    }

    @Override
    public Collection<V> values() {
        reap();
        return this.backingStore.values();
    }

    @SuppressWarnings("unchecked")
    private synchronized void reap() {
        Object zombie = this.queue.poll();

        while (zombie != null) {
            IdentityWeakReference victim = (IdentityWeakReference) zombie;
            this.backingStore.remove(victim);
            zombie = this.queue.poll();
        }
    }

    class IdentityWeakReference extends WeakReference<K> {

        int hash;

        @SuppressWarnings("unchecked")
        IdentityWeakReference(Object obj) {
            super((K) obj, WeakIdentityHashMap.this.queue);
            this.hash = System.identityHashCode(obj);
        }

        @Override
        public int hashCode() {
            return this.hash;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof WeakIdentityHashMap.IdentityWeakReference)) {
                return false;
            }
            IdentityWeakReference ref = (IdentityWeakReference) o;
            return this.get() == ref.get();
        }
    }
}
