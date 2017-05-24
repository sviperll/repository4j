package com.github.sviperll.repository4j;

import com.github.sviperll.repository4j.SQLBiFunction;
import com.github.sviperll.repository4j.jdbcwrapper.PreparedQuery;
import com.github.sviperll.repository4j.jdbcwrapper.Query;
import com.github.sviperll.repository4j.jdbcwrapper.QueryResult;
import com.github.sviperll.repository4j.jdbcwrapper.Transaction;
import com.github.sviperll.repository4j.jdbcwrapper.rawlayout.RowLayout;
import com.github.sviperll.repository4j.provider.SQLHelper;
import com.github.sviperll.repository4j.provider.SQLProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RepositoryMethodFactory {
    private static final RowLayout<Integer> LIMIT_ROW_LAYOUT = RowLayout.integerColumn(SQLHelper.LIMIT_COLUMN_NAME);

    private final SQLProvider provider;

    public RepositoryMethodFactory(SQLProvider provider) {
        this.provider = provider;
    }

    public <K, V> DisconnectedSQLFunction<K, Optional<V>> get(String tableName,
                                                              RowLayout<K> key,
                                                              RowLayout<V> entry) {
        List<String> entryColumns = entry.getColumnNames();
        List<String> keyColumns = key.getColumnNames();
        List<String> columnsToSelect = new ArrayList<>(entryColumns);
        columnsToSelect.removeAll(keyColumns);

        Query query = provider.get(tableName, keyColumns, columnsToSelect);
        return (Transaction transaction) -> {
            return (K keyValue) -> {
                try (PreparedQuery preparedQuery = transaction.prepareQuery(query)) {
                    preparedQuery.fillIn(key, keyValue);
                    Query.Executor<V> queryExecutor = preparedQuery.createQueryExecutor(entry);
                    queryExecutor.setConstantColumns(key, keyValue);
                    try (QueryResult<V> result = queryExecutor.execute()) {
                        if (!result.next()) {
                            return Optional.empty();
                        } else {
                            return Optional.of(result.get());
                        }
                    }
                }
            };
        };
    }

    public <E, O> DisconnectedSQLFunction<QuerySlicing<O>, List<E>> entryList(String tableName,
                                                                              RowLayout<E> entry,
                                                                              RowLayout<O> ordering) {

        DisconnectedSQLBiFunction<Void, QuerySlicing<O>, List<E>> disconnected =
                entryList(tableName, entry, RowLayout.nothing(), ordering);
        return (Transaction transaction) -> {
            SQLBiFunction<Void, QuerySlicing<O>, List<E>> method = disconnected.getSQLBiFunction(transaction);
            return (QuerySlicing<O> slicing) -> {
                return method.apply(null, slicing);
            };
        };
    }

    public <K, E, O> DisconnectedSQLBiFunction<K, QuerySlicing<O>, List<E>> entryList(String tableName,
                                                                                      RowLayout<E> entry,
                                                                                      RowLayout<K> partitionKey,
                                                                                      RowLayout<O> ordering) {
        List<String> entryColumns = entry.getColumnNames();
        List<String> orderColumns = ordering.getColumnNames();
        List<String> partitionKeyColumns = partitionKey.getColumnNames();
        List<String> columnsToSelect = new ArrayList<>(entryColumns);
        columnsToSelect.removeAll(partitionKeyColumns);

        Map<QuerySlicing.Kind, Query> queries = Stream.of(QuerySlicing.Kind.values())
                .collect(Collectors.toMap(Function.identity(), kind -> {
                    return provider.entryList(tableName, columnsToSelect, partitionKeyColumns, orderColumns, kind);
                }));
        return (Transaction transaction) -> {
            return (K partitionKeyValue, QuerySlicing<O> slicing) -> {
                QuerySlicing.Kind kind = slicing.kind();
                Query query = Optional.ofNullable(queries.get(slicing.kind()))
                        .orElseThrow(() -> new IllegalStateException("Unprepared slicing " + kind));
                try (PreparedQuery preparedQuery = transaction.prepareQuery(query)) {
                    preparedQuery.fillIn(partitionKey, partitionKeyValue);
                    if (kind.shouldHaveCondition()) {
                        preparedQuery.fillIn(ordering, slicing.getComparisonValue());
                    }
                    if (kind.shouldBeLimited()) {
                        preparedQuery.fillIn(LIMIT_ROW_LAYOUT, slicing.getLimit());
                    }
                    Query.Executor<E> queryExecutor = preparedQuery.createQueryExecutor(entry);
                    queryExecutor.setConstantColumns(partitionKey, partitionKeyValue);
                    try (QueryResult<E> resultSet = queryExecutor.execute()) {
                        List<E> result = new ArrayList<>();
                        while (resultSet.next()) {
                            result.add(resultSet.get());
                        }
                        if (kind.shouldBeOrdered() && kind.orderIsReversed())
                            Collections.reverse(result);
                        return result;
                    }
                }
            };
        };
    }
}
