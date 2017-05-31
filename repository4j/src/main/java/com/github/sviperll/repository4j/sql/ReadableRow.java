package com.github.sviperll.repository4j.sql;

import javax.annotation.Nullable;
import java.sql.SQLException;

public interface ReadableRow {
    @Nullable
    Integer getInteger(String columnName) throws SQLException;

    @Nullable
    Long getLong(String columnName) throws SQLException;

    @Nullable
    String getString(String columnName) throws SQLException;
}
