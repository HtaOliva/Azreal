package br.senac.fullstack.service;

import org.springframework.beans.factory.ObjectProvider;

public class NullObjectProvider<T> implements ObjectProvider<T> {
    @Override
    public T getObject() { return null; }
    @Override
    public T getObject(Object... args) { return null; }
    @Override
    public T getIfAvailable() { return null; }
    @Override
    public T getIfUnique() { return null; }
    @Override
    public java.util.Iterator<T> iterator() { return java.util.Collections.emptyIterator(); }
}
