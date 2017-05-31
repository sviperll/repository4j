package com.github.sviperll.repository4j.sql;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLConsumer<T> {
    void accept(T value) throws SQLException;
}
