package com.github.sviperll.repository4j.jdbcwrapper;

import com.github.sviperll.repository4j.jdbcwrapper.rawlayout.RowLayout;
import com.github.sviperll.repository4j.sql.SQLConsumer;
import com.github.sviperll.repository4j.sql.SQLSupplier;
import com.github.sviperll.repository4j.sql.WritableRaw;

import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

public class QueryResult<T> implements AutoCloseable {
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

    public static class Extractor<T> {
        private final RowLayout<T> resultRowLayout;
        private final SQLSupplier<ReadableResult> resultSetFactory;
        private final KnownValues knownValues = new KnownValues();

        Extractor(RowLayout<T> resultRowLayout, SQLSupplier<ReadableResult> resultSetFactory) {
            this.resultRowLayout = resultRowLayout;
            this.resultSetFactory = resultSetFactory;
        }

        public QueryResult<T> extractQueryResult() throws SQLException {
            try {
                return knownValues.createQueryResult(resultRowLayout, resultSetFactory);
            } catch (SQLException e) {
                throw SQLExceptions.precise(e);
            }
        }

        public <K> void setConstantColumns(RowLayout<K> rowLayout, K value)
                throws SQLException {

            SQLConsumer<K> setter = rowLayout.createRawWriter(knownValues);
            try {
                setter.accept(value);
            } catch (SQLException e) {
                throw SQLExceptions.precise(e);
            }
        }

        private static class KnownValues implements WritableRaw {
            private final Map<String, Object> values = new TreeMap<>();
            @Override
            public void setNull(String columnName, int sqlType) throws SQLException {
                values.put(columnName, null);
            }

            @Override
            public void setInt(String columnName, int value) throws SQLException {
                values.put(columnName, value);
            }

            @Override
            public void setLong(String columnName, long value) throws SQLException {
                values.put(columnName, value);
            }

            @Override
            public void setString(String columnName, String value) throws SQLException {
                values.put(columnName, value);
            }

            <T> QueryResult<T> createQueryResult(RowLayout<T> resultRowLayout,
                                                 SQLSupplier<ReadableResult> resultSetFactory) throws SQLException {
                ReadableResult readableResult = resultSetFactory.get();
                readableResult = values.isEmpty()
                        ? readableResult
                        : new ReadableResult.WithConstantColumns(values, readableResult);
                return new QueryResult<>(resultRowLayout, readableResult);
            }
        }

    }
}
