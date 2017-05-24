package com.github.sviperll.repository4j.jdbcwrapper.rawlayout;

import com.github.sviperll.repository4j.SQLConsumer;
import com.github.sviperll.repository4j.SQLSupplier;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @param <T>
 */
public abstract class RowLayout<T> {

    private static final VoidRowLayout NOTHING = new VoidRowLayout();

    public static <T, B> BuilderRowLayoutBuilder<T, B> forBuilder(Supplier<B> builderFactory,
                                                                  Function<B, T> buildMethod) {
        return new BuilderRowLayoutBuilder<>(builderFactory, buildMethod);
    }

    public static <T, ARG1, ARG2> ConstructorRowLayoutBuilder<T, ARG1, ARG2> forConstructor(BiFunction<ARG1, ARG2, T> constructor) {
        return new ConstructorRowLayoutBuilder<>(constructor);
    }

    public static RowLayout<Integer> integerColumn(String columnName) {
        return new Cell.IntegerCell(columnName);
    }

    public static RowLayout<Long> longColumn(String columnName) {
        return new Cell.LongCell(columnName);
    }

    public static RowLayout<String> stringColumn(String columnName) {
        return new Cell.StringCell(columnName);
    }

    public static RowLayout<Void> nothing() {
        return NOTHING;
    }

    RowLayout() {
    }

    public abstract List<String> getColumnNames();

    public abstract SQLSupplier<T> createRawReader(ReadableRow readable);

    public abstract SQLConsumer<T> createRawWriter(WritableRaw query);

    public <U> RowLayout<U> morph(Function<T, U> forward, Function<U, T> backward) {
        return new IsomorphicRowLayout<>(this, forward, backward);
    }

    private static class VoidRowLayout extends RowLayout<Void> {
        @Override
        public List<String> getColumnNames() {
            return Collections.emptyList();
        }

        @Override
        public SQLSupplier<Void> createRawReader(ReadableRow readable) {
            return () -> {
                return null;
            };
        }

        @Override
        public SQLConsumer<Void> createRawWriter(WritableRaw query) {
            return (value) -> {
            };
        }
    }
}
