package com.github.sviperll.repository4j;

import com.github.sviperll.repository4j.SQLBiFunction;
import com.github.sviperll.repository4j.jdbcwrapper.Transaction;

@FunctionalInterface
public interface DisconnectedSQLBiFunction<T, U, R> {
    SQLBiFunction<T, U, R> getSQLBiFunction(Transaction transaction);
}
