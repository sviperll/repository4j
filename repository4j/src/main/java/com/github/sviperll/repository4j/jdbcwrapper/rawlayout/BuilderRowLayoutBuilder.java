package com.github.sviperll.repository4j.jdbcwrapper.rawlayout;

import com.github.sviperll.repository4j.SQLConsumer;
import com.github.sviperll.repository4j.SQLRunnable;
import com.github.sviperll.repository4j.SQLSupplier;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BuilderRowLayoutBuilder<T, B> {
    private final Supplier<B> builderFactory;
    private final Function<B, T> buildMethod;
    private final List<Component<T, B, ?>> components = new ArrayList<>();

    BuilderRowLayoutBuilder(Supplier<B> builderFactory, Function<B, T> buildMethod) {
        this.builderFactory = builderFactory;
        this.buildMethod = buildMethod;
    }

    public void addInt(String columnName, Function<T, Integer> getter, BiConsumer<B, Integer> setter) {
        components.add(new Component<>(RowLayout.integerColumn(columnName), getter, setter));
    }
    public void addLong(String columnName, Function<T, Long> getter, BiConsumer<B, Long> setter) {
        components.add(new Component<>(RowLayout.longColumn(columnName), getter, setter));
    }
    public void addString(String columnName, Function<T, String> getter, BiConsumer<B, String> setter) {
        components.add(new Component<>(RowLayout.stringColumn(columnName), getter, setter));
    }
    public <U> void addStorable(RowLayout<U> storable, Function<T, U> getter, BiConsumer<B, U> setter) {
        components.add(new Component<>(storable, getter, setter));
    }

    public RowLayout<T> build() {
        return new BuilderRowLayout<>(components, builderFactory, buildMethod);
    }

    private static class Component<T, B, SUB> {
        private final RowLayout<SUB> storable;
        private final Function<T, SUB> getter;
        private final BiConsumer<B, SUB> setter;

        Component(RowLayout<SUB> storable, Function<T, SUB> getter, BiConsumer<B, SUB> setter) {
            this.storable = storable;
            this.getter = getter;
            this.setter = setter;
        }

        List<String> getColumnNames() {
            return storable.getColumnNames();
        }

        SQLRunnable createComponentReader(ReadableRow readable, B builder) {
            return () -> {
                SQLSupplier<SUB> instanceLoader = storable.createRawReader(readable);
                SUB sub = instanceLoader.get();
                setter.accept(builder, sub);
            };
        }

        SQLConsumer<T> createComponentWriter(WritableRaw query) {
            SQLConsumer<SUB> saver = storable.createRawWriter(query);
            return value -> {
                saver.accept(getter.apply(value));
            };
        }
    }

    private static class Loader<T, B> {
        private final ReadableRow readable;
        private final B builder;

        Loader(ReadableRow readable, B builder) {
            this.readable = readable;
            this.builder = builder;
        }

        void readComponents(List<Component<T, B, ?>> components) throws SQLException {
            for (Component<T, B, ?> component: components) {
                SQLRunnable reader = component.createComponentReader(readable, builder);
                reader.run();
            }
        }
    }

    private static class BuilderRowLayout<T, B> extends RowLayout<T> {
        private final List<Component<T, B, ?>> components;
        private final Supplier<B> builderFactory;
        private final Function<B, T> buildMethod;
        private final List<String> columnNames;

        BuilderRowLayout(List<Component<T, B, ?>> components,
                                 Supplier<B> builderFactory,
                                 Function<B, T> buildMethod) {
            this.components = Collections.unmodifiableList(new ArrayList<>(components));
            this.builderFactory = builderFactory;
            this.buildMethod = buildMethod;
            columnNames = components.stream()
                    .flatMap(component -> component.getColumnNames().stream())
                    .collect(Collectors.toList());
        }

        @Override
        public List<String> getColumnNames() {
            return columnNames;
        }

        @Override
        public SQLSupplier<T> createRawReader(ReadableRow readable) {
            return () -> {
                B builder = builderFactory.get();
                Loader<T, B> loader = new Loader<>(readable, builder);
                loader.readComponents(components);
                return buildMethod.apply(builder);
            };
        }

        @Override
        public SQLConsumer<T> createRawWriter(WritableRaw writable) {
            List<SQLConsumer<T>> writers = components.stream()
                    .map(component -> component.createComponentWriter(writable))
                    .collect(Collectors.toList());
            return value -> {
                for (SQLConsumer<T> saver: writers) {
                    saver.accept(value);
                }
            };
        }

    }
}
