package com.github.sviperll.repository4j;

import com.github.sviperll.repository4j.jdbcwrapper.Transaction;

@FunctionalInterface
public interface SQLDisconnectedRepositoryFunction<T, R> {
    RepositoryFunction<T, R> getRepositoryFunction(Transaction transaction);
}
