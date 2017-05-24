package com.github.sviperll.repository4j;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLSupplier<T> {
    T get() throws SQLException;
}
