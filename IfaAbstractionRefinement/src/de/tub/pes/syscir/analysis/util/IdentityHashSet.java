package de.tub.pes.syscir.analysis.util;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

public class IdentityHashSet<E> implements Set<E> {

    private final IdentityHashMap<E, Object> map;

    public IdentityHashSet() {
        this.map = new IdentityHashMap<>();
    }

    public IdentityHashSet(Collection<E> initialElements) {
        this();
        addAll(initialElements);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public Object[] toArray() {
        return map.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return map.keySet().toArray(a);
    }

    @Override
    public boolean add(E e) {
        return map.put(e, this) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object element : c) {
            if (!contains(element))
                return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean changed = false;
        for (E element : c)
            changed |= add(element);
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return map.keySet().retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return map.keySet().removeAll(c);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public String toString() {
        return map.keySet().toString();
    }

    @Override
    public int hashCode() {
        return map.keySet().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != getClass())
            return false;
        return map.keySet().equals(((IdentityHashSet<?>) obj).map.keySet());
    }
}
