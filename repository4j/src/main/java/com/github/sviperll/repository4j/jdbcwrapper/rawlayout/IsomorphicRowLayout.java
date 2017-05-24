package com.github.sviperll.repository4j.jdbcwrapper.rawlayout;

import com.github.sviperll.repository4j.SQLConsumer;
import com.github.sviperll.repository4j.SQLSupplier;

import java.util.List;
import java.util.function.Function;

class IsomorphicRowLayout<T, U> extends RowLayout<T> {
    private final RowLayout<U> base;
    private final Function<U, T> forward;
    private final Function<T, U> backward;

    IsomorphicRowLayout(RowLayout<U> base, Function<U, T> forward, Function<T, U> backward) {
        this.base = base;
        this.forward = forward;
        this.backward = backward;
    }

    @Override
    public List<String> getColumnNames() {
        return base.getColumnNames();
    }

    @Override
    public SQLSupplier<T> createRawReader(ReadableRow readable) {
        SQLSupplier<U> reader = base.createRawReader(readable);
        return () -> {
            return forward.apply(reader.get());
        };
    }

    @Override
    public SQLConsumer<T> createRawWriter(WritableRaw query) {
        SQLConsumer<U> writer = base.createRawWriter(query);
        return value -> {
            writer.accept(backward.apply(value));
        };
    }
}
