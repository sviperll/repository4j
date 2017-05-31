package com.github.sviperll.repository4j.jdbcwrapper.rawlayout;

import com.github.sviperll.repository4j.sql.ReadableRow;
import com.github.sviperll.repository4j.sql.SQLConsumer;
import com.github.sviperll.repository4j.sql.SQLSupplier;
import com.github.sviperll.repository4j.sql.WritableRaw;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConstructorRowLayoutBuilder<T, ARG1, ARG2> {
    private final BiFunction<ARG1, ARG2, T> constructor;
    private RowLayout<ARG1> storable1;
    private Function<T, ARG1> getter1;
    private RowLayout<ARG2> storable2;
    private Function<T, ARG2> getter2;

    ConstructorRowLayoutBuilder(BiFunction<ARG1, ARG2, T> constructor) {
        this.constructor = constructor;
    }

    public void setComponent1(RowLayout<ARG1> storable, Function<T, ARG1> getter) {
        Objects.requireNonNull(storable);
        Objects.requireNonNull(getter);
        this.storable1 = storable;
        this.getter1 = getter;
    }

    public void setComponent2(RowLayout<ARG2> storable, Function<T, ARG2> getter) {
        Objects.requireNonNull(storable);
        Objects.requireNonNull(getter);
        this.storable2 = storable;
        this.getter2 = getter;
    }

    public RowLayout<T> build() {
        return new ConstructorRowLayout<>(this);
    }

    private static class ConstructorRowLayout<T, ARG1, ARG2> extends RowLayout<T> {

        private final BiFunction<ARG1, ARG2, T> constructor;
        private final RowLayout<ARG1> storable1;
        private final Function<T, ARG1> getter1;
        private final RowLayout<ARG2> storable2;
        private final Function<T, ARG2> getter2;
        private final List<String> columnNames;

        private ConstructorRowLayout(ConstructorRowLayoutBuilder<T, ARG1, ARG2> builder) {
            this.constructor = builder.constructor;
            this.storable1 = builder.storable1;
            this.getter1 = builder.getter1;
            this.storable2 = builder.storable2;
            this.getter2 = builder.getter2;
            this.columnNames = Stream.concat(
                    storable1.getColumnNames().stream(),
                    storable2.getColumnNames().stream()).collect(Collectors.toList());
        }

        @Override
        public List<String> getColumnNames() {
            return columnNames;
        }

        @Override
        public SQLSupplier<T> createRawReader(ReadableRow readable) {
            SQLSupplier<ARG1> reader1 = storable1.createRawReader(readable);
            SQLSupplier<ARG2> reader2 = storable2.createRawReader(readable);
            return () -> {
                ARG1 arg1 = reader1.get();
                ARG2 arg2 = reader2.get();
                return constructor.apply(arg1, arg2);
            };
        }

        @Override
        public SQLConsumer<T> createRawWriter(WritableRaw query) {
            SQLConsumer<ARG1> writer1 = storable1.createRawWriter(query);
            SQLConsumer<ARG2> writer2 = storable2.createRawWriter(query);
            return value -> {
                ARG1 arg1 = getter1.apply(value);
                writer1.accept(arg1);
                ARG2 arg2 = getter2.apply(value);
                writer2.accept(arg2);
            };
        }


    }
}
