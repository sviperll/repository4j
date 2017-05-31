package com.github.sviperll.repository4j.sql;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLFunction<T, R> {
    R apply(T argument) throws SQLException;
}
