package de.tub.pes.syscir.analysis.util;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;


public abstract class WrappedMap<K, V> implements Map<K, V> {

    public static <K, V> WrappedMap<K, V> replaceInsertedKeys(Map<K, V> original, Function<K, K> replacementFunction) {
        return new WrappedMap<>(original) {

            @Override
            public V put(K key, V value) {
                return this.original.put(replacementFunction.apply(key), value);
            }

            @Override
            public void putAll(Map<? extends K, ? extends V> m) {
                for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
                    put(entry.getKey(), entry.getValue());
                }
            }

            @Override
            public V putIfAbsent(K key, V value) {
                return this.original.putIfAbsent(replacementFunction.apply(key), value);
            }

            @Override
            public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
                return this.original.computeIfAbsent(replacementFunction.apply(key), mappingFunction);
            }

            @Override
            public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
                return this.original.compute(replacementFunction.apply(key), remappingFunction);
            }

            @Override
            public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
                return this.original.merge(replacementFunction.apply(key), value, remappingFunction);
            }
        };
    }

    protected final Map<K, V> original;

    public WrappedMap(Map<K, V> original) {
        this.original = Objects.requireNonNull(original);
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
    public boolean containsKey(Object key) {
        return this.original.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.original.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.original.get(key);
    }

    @Override
    public V put(K key, V value) {
        return this.original.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return this.original.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.original.putAll(m);
    }

    @Override
    public void clear() {
        this.original.clear();
    }

    @Override
    public Set<K> keySet() {
        return this.original.keySet();
    }

    @Override
    public Collection<V> values() {
        return this.original.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.original.entrySet();
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
    public V getOrDefault(Object key, V defaultValue) {
        return this.original.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        this.original.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        this.original.replaceAll(function);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return this.original.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return this.original.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return this.original.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return this.original.replace(key, value);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return this.original.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return this.original.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return this.original.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return this.original.merge(key, value, remappingFunction);
    }

}
