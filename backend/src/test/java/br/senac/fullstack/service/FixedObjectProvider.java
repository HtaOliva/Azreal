package br.senac.fullstack.service;

import org.springframework.beans.factory.ObjectProvider;

public class FixedObjectProvider<T> implements ObjectProvider<T> {
    private final T value;
    public FixedObjectProvider(T value) { this.value = value; }
    @Override public T getObject() { return value; }
    @Override public T getObject(Object... args) { throw new UnsupportedOperationException("not implemented"); }
    @Override public T getIfAvailable() { return getObject(); }
    @Override public T getIfUnique() { return null; }
    @Override public java.util.Iterator<T> iterator() { return java.util.Collections.singleton(value).iterator(); }
}
