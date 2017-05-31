package com.github.sviperll.repository4j;

@FunctionalInterface
public interface RepositoryBiFunction<T, U, R> {
    R apply(T argument1, U argument2) throws RepositoryException, TransientTransactionException;
}
