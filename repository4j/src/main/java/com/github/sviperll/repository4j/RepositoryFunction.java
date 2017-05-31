package com.github.sviperll.repository4j;

@FunctionalInterface
public interface RepositoryFunction<T, R> {
    R apply(T argument) throws RepositoryException, TransientTransactionException;
}
