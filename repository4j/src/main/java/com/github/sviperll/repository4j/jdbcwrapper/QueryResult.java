package com.github.sviperll.repository4j.jdbcwrapper;

import com.github.sviperll.repository4j.jdbcwrapper.rawlayout.RowLayout;
import com.github.sviperll.repository4j.sql.ReadableRow;
import com.github.sviperll.repository4j.sql.SQLSupplier;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class QueryResult<T> implements AutoCloseable {
    static <T> QueryResult<T> createInstance(RowLayout<T> rowLayout,
                                             Map<String, Object> constantColumns,
                                             ResultSet resultSet) {
        return new QueryResult<>(rowLayout, new ReadableResult(constantColumns, resultSet));
    }

    private final RowLayout<T> rowLayout;
    private final ReadableResult readableResult;

    private QueryResult(RowLayout<T> rowLayout, ReadableResult readableResult) {
        this.rowLayout = rowLayout;
        this.readableResult = readableResult;
    }

    @Override
    public void close() throws SQLException {
        try {
            readableResult.close();
        } catch (SQLException e) {
            throw SQLExceptions.precise(e);
        }
    }

    public boolean next() throws SQLException {
        try {
            return readableResult.next();
        } catch (SQLException e) {
            throw SQLExceptions.precise(e);
        }
    }

    public T get() throws SQLException {
        SQLSupplier<T> instanceLoader = rowLayout.createRawReader(readableResult);
        try {
            return instanceLoader.get();
        } catch (SQLException e) {
            throw SQLExceptions.precise(e);
        }
    }

    private static class ReadableResult implements ReadableRow, AutoCloseable {
        private final Map<String, Object> values;
        private final ResultSet resultSet;

        ReadableResult(Map<String, Object> values, ResultSet resultSet) {
            this.values = values;
            this.resultSet = resultSet;
        }

        public boolean next() throws SQLException {
            return resultSet.next();
        }

        @Nullable
        @Override
        public Integer getInteger(String columnName) throws SQLException {
            if (values.containsKey(columnName)) {
                return (Integer)values.get(columnName);
            } else {
                int value = resultSet.getInt(columnName);
                return resultSet.wasNull() ? null : value;
            }
        }

        @Nullable
        @Override
        public Long getLong(String columnName) throws SQLException {
            if (values.containsKey(columnName)) {
                return (Long)values.get(columnName);
            } else {
                long value = resultSet.getLong(columnName);
                return resultSet.wasNull() ? null : value;
            }
        }

        @Nullable
        @Override
        public String getString(String columnName) throws SQLException {
            if (values.containsKey(columnName)) {
                return (String)values.get(columnName);
            } else {
                return resultSet.getString(columnName);
            }
        }

        @Override
        public void close() throws SQLException {
            resultSet.close();
        }
    }
}
