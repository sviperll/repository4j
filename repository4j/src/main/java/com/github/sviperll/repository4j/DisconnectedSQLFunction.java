package com.github.sviperll.repository4j;

import com.github.sviperll.repository4j.SQLFunction;
import com.github.sviperll.repository4j.jdbcwrapper.Transaction;

@FunctionalInterface
public interface DisconnectedSQLFunction<T, R> {
    SQLFunction<T, R> getSQLFunction(Transaction transaction);
}
