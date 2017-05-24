package com.github.sviperll.repository4j.jdbcwrapper.rawlayout;

import com.github.sviperll.repository4j.SQLConsumer;
import com.github.sviperll.repository4j.SQLSupplier;

import java.sql.Types;
import java.util.Collections;
import java.util.List;

abstract class Cell<T> extends RowLayout<T> {
    private final String columnName;

    private Cell(String columnName) {
        this.columnName = columnName;
    }

    String columnName() {
        return columnName;
    }

    @Override
    public final List<String> getColumnNames() {
        return Collections.singletonList(columnName);
    }


    static class IntegerCell extends Cell<Integer> {
        IntegerCell(String columnName) {
            super(columnName);
        }

        @Override
        public SQLSupplier<Integer> createRawReader(ReadableRow readable) {
            return () -> {
                return readable.getInteger(columnName());
            };
        }

        @Override
        public SQLConsumer<Integer> createRawWriter(WritableRaw query) {
            return (value) -> {
                if (value == null)
                    query.setNull(columnName(), Types.INTEGER);
                else
                    query.setInt(columnName(), value);
            };
        }
    }

    static class LongCell extends Cell<Long> {
        LongCell(String columnName) {
            super(columnName);
        }

        @Override
        public SQLSupplier<Long> createRawReader(ReadableRow readable) {
            return () -> {
                return readable.getLong(columnName());
            };
        }

        @Override
        public SQLConsumer<Long> createRawWriter(WritableRaw query) {
            return (value) -> {
                if (value == null)
                    query.setNull(columnName(), Types.BIGINT);
                else
                    query.setLong(columnName(), value);
            };
        }
    }

    static class StringCell extends Cell<String> {
        StringCell(String columnName) {
            super(columnName);
        }

        @Override
        public SQLSupplier<String> createRawReader(ReadableRow readable) {
            return () -> {
                return readable.getString(columnName());
            };
        }

        @Override
        public SQLConsumer<String> createRawWriter(WritableRaw query) {
            return (value) -> {
                if (value == null)
                    query.setNull(columnName(), Types.VARCHAR);
                else
                    query.setString(columnName(), value);
            };
        }
    }
}
