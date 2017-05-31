package com.github.sviperll.repository4j.sql;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLBiFunction<T, U, R> {
    R apply(T argument1, U argument2) throws SQLException;
}
