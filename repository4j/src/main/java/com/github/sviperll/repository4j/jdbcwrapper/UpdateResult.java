package com.github.sviperll.repository4j.jdbcwrapper;

import com.github.sviperll.repository4j.jdbcwrapper.rawlayout.RowLayout;
import com.github.sviperll.repository4j.sql.SQLSupplier;

public class UpdateResult {
    private final int count;
    private final SQLSupplier<ReadableResult> resultSetFactory;

    UpdateResult(int count, SQLSupplier<ReadableResult> resultSetFactory) {
        this.count = count;
        this.resultSetFactory = resultSetFactory;
    }

    public int count() {
        return count;
    }

    public <T> QueryResult.Extractor<T> createGeneratedResultExtractor(RowLayout<T> resultRowLayout) {
        return new QueryResult.Extractor<>(resultRowLayout, resultSetFactory);
    }
}
