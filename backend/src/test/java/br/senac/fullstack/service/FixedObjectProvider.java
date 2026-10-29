package br.senac.fullstack.service;

import org.springframework.beans.factory.ObjectProvider;

public class FixedObjectProvider<T> implements ObjectProvider<T> {
    private final T value;
    public FixedObjectProvider(T value) { this.value = value; }
    @Override public T getObject() { return value; }
    @Override public T getObject(Object... args) { return (args == null || args.length == 0) ? value : value; }
    @Override public T getIfAvailable() { return value; }
    @Override public T getIfUnique() { return value; }
    @Override public java.util.Iterator<T> iterator() { return java.util.Collections.singleton(value).iterator(); }
}
