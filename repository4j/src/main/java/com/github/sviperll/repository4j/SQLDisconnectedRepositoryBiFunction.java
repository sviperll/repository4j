package com.github.sviperll.repository4j;

import com.github.sviperll.repository4j.jdbcwrapper.Transaction;

@FunctionalInterface
public interface SQLDisconnectedRepositoryBiFunction<T, U, R> {
    RepositoryBiFunction<T, U, R> getRepositoryBiFunction(Transaction transaction);
}
