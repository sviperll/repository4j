package com.github.sviperll.repository4j.sql;

import java.sql.SQLException;

public interface WritableRaw {
    void setNull(String columnName, int sqlType) throws SQLException;

    void setInt(String columnName, int value) throws SQLException;

    void setLong(String columnName, long value) throws SQLException;

    void setString(String columnName, String value) throws SQLException;
}
