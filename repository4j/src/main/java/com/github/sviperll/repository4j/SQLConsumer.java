package com.github.sviperll.repository4j;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLConsumer<T> {
    void accept(T value) throws SQLException;
}
