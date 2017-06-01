package com.github.sviperll.repository4j.jdbcwrapper;

import com.github.sviperll.repository4j.sql.ReadableRow;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

interface ReadableResult extends ReadableRow, AutoCloseable {
    boolean next() throws SQLException;

    @Override
    void close() throws SQLException;

    class EmptyRows implements ReadableResult {
        private final int count;
        private int current = 0;

        EmptyRows(int count) {
            this.count = count;
        }

        @Nullable
        @Override
        public Integer getInteger(String columnName) throws SQLException {
            throw new SQLException("Missing column " + columnName);
        }

        @Nullable
        @Override
        public Long getLong(String columnName) throws SQLException {
            throw new SQLException("Missing column " + columnName);
        }

        @Nullable
        @Override
        public String getString(String columnName) throws SQLException {
            throw new SQLException("Missing column " + columnName);
        }

        @Override
        public boolean next() throws SQLException {
            boolean result = current < count;
            current++;
            return result;
        }

        @Override
        public void close() throws SQLException {
        }
    }
    class WithConstantColumns implements ReadableResult {
        private final Map<String, Object> values;
        private final ReadableResult result;

        WithConstantColumns(Map<String, Object> values, ReadableResult result) {
            this.values = Collections.unmodifiableMap(new TreeMap<>(values));
            this.result = result;
        }

        @Override
        public boolean next() throws SQLException {
            return result.next();
        }

        @Nullable
        @Override
        public Integer getInteger(String columnName) throws SQLException {
            if (values.containsKey(columnName)) {
                return (Integer)values.get(columnName);
            } else {
                return result.getInteger(columnName);
            }
        }

        @Nullable
        @Override
        public Long getLong(String columnName) throws SQLException {
            if (values.containsKey(columnName)) {
                return (Long)values.get(columnName);
            } else {
                return result.getLong(columnName);
            }
        }

        @Nullable
        @Override
        public String getString(String columnName) throws SQLException {
            if (values.containsKey(columnName)) {
                return (String)values.get(columnName);
            } else {
                return result.getString(columnName);
            }
        }

        @Override
        public void close() throws SQLException {
            result.close();
        }
    }

    class OfResultSet implements ReadableResult {
        private final ResultSet resultSet;

        OfResultSet(ResultSet resultSet) {
            this.resultSet = resultSet;
        }

        @Override
        public boolean next() throws SQLException {
            return resultSet.next();
        }

        @Nullable
        @Override
        public Integer getInteger(String columnName) throws SQLException {
            int value = resultSet.getInt(columnName);
            return resultSet.wasNull() ? null : value;
        }

        @Nullable
        @Override
        public Long getLong(String columnName) throws SQLException {
            long value = resultSet.getLong(columnName);
            return resultSet.wasNull() ? null : value;
        }

        @Nullable
        @Override
        public String getString(String columnName) throws SQLException {
            return resultSet.getString(columnName);
        }

        @Override
        public void close() throws SQLException {
            resultSet.close();
        }
    }

}
