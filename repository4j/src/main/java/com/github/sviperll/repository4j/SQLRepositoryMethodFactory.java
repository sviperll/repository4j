package com.github.sviperll.repository4j;

import com.github.sviperll.repository4j.jdbcwrapper.PreparedQuery;
import com.github.sviperll.repository4j.jdbcwrapper.Query;
import com.github.sviperll.repository4j.jdbcwrapper.QueryResult;
import com.github.sviperll.repository4j.jdbcwrapper.Transaction;
import com.github.sviperll.repository4j.jdbcwrapper.UpdateResult;
import com.github.sviperll.repository4j.jdbcwrapper.rawlayout.RowLayout;

import java.sql.SQLException;
import java.sql.SQLTransientException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SQLRepositoryMethodFactory {
    private static final RowLayout<Integer> LIMIT_ROW_LAYOUT = RowLayout.integerColumn(QueryFactories.LIMIT_COLUMN_NAME);

    private final QueryFactory provider;

    public SQLRepositoryMethodFactory(QueryFactory provider) {
        this.provider = provider;
    }

    public <K, V> SQLDisconnectedRepositoryFunction<K, Optional<V>> get(String tableName,
                                                                        RowLayout<K> key,
                                                                        RowLayout<V> entry) {
        List<String> entryColumns = entry.getColumnNames();
        List<String> keyColumns = key.getColumnNames();
        List<String> selectedColumns = new ArrayList<>(entryColumns);
        selectedColumns.removeAll(keyColumns);

        Query query = provider.getQuery(tableName, keyColumns, selectedColumns);
        return (Transaction transaction) -> {
            return (K keyValue) -> {
                try (PreparedQuery preparedQuery = transaction.prepareQuery(query)) {
                    preparedQuery.fillIn(key, keyValue);
                    QueryResult.Extractor<V> queryExtractor = preparedQuery.createQueryResultExtractor(entry);
                    queryExtractor.setConstantColumns(key, keyValue);
                    try (QueryResult<V> result = queryExtractor.extractQueryResult()) {
                        if (!result.next()) {
                            return Optional.empty();
                        } else {
                            return Optional.of(result.get());
                        }
                    }
                } catch (SQLTransientException ex) {
                    throw new SQLTransientTransactionException(ex);
                } catch (SQLException ex) {
                    throw new SQLRepositoryException(ex);
                }
            };
        };
    }

    public <V, E> SQLDisconnectedRepositoryFunction<V, E> put(String tableName,
                                                              RowLayout<V> value,
                                                              RowLayout<E> fullEntry,
                                                              Map<String, String> columnExpressions) {
        List<String> valueColumns = value.getColumnNames();
        List<String> entryColumns = fullEntry.getColumnNames();
        List<String> generatedColumns = new ArrayList<>(entryColumns);
        generatedColumns.removeAll(valueColumns);

        Query query = provider.putQuery(tableName, valueColumns, columnExpressions);
        return (Transaction transaction) -> {
            return (V actualValue) -> {
                try (PreparedQuery preparedQuery = transaction.prepareQuery(query, generatedColumns)) {
                    preparedQuery.fillIn(value, actualValue);
                    UpdateResult updateResult = preparedQuery.executeUpdate();
                    if (updateResult.count() != 1) {
                        throw new SQLRepositoryException(
                                MessageFormat.format("Expecting single inserted row, but got {0}",
                                        updateResult.count()));
                    } else {
                        QueryResult.Extractor<E> generatedExtractor =
                                updateResult.createGeneratedResultExtractor(fullEntry);
                        generatedExtractor.setConstantColumns(value, actualValue);
                        try (QueryResult<E> result = generatedExtractor.extractQueryResult()) {
                            if (!result.next()) {
                                throw new SQLRepositoryException("Unable to access generated values in inserted row");
                            } else {
                                return result.get();
                            }
                        }
                    }
                } catch (SQLTransientException ex) {
                    throw new SQLTransientTransactionException(ex);
                } catch (SQLException ex) {
                    throw new SQLRepositoryException(ex);
                }
            };
        };
    }

    public <E, O> SQLDisconnectedRepositoryFunction<QuerySlicing<O>, List<E>> entryList(String tableName,
                                                                                        RowLayout<E> entry,
                                                                                        RowLayout<O> ordering) {

        SQLDisconnectedRepositoryBiFunction<Void, QuerySlicing<O>, List<E>> disconnected =
                entryList(tableName, entry, RowLayout.nothing(), ordering);
        return (Transaction transaction) -> {
            RepositoryBiFunction<Void, QuerySlicing<O>, List<E>> method =
                    disconnected.getRepositoryBiFunction(transaction);
            return (QuerySlicing<O> slicing) -> {
                return method.apply(null, slicing);
            };
        };
    }

    public <K, E, O> SQLDisconnectedRepositoryBiFunction<K, QuerySlicing<O>, List<E>> entryList(String tableName,
                                                                                                RowLayout<E> entry,
                                                                                                RowLayout<K> filteredByKey,
                                                                                                RowLayout<O> orderBy) {
        List<String> entryColumns = entry.getColumnNames();
        List<String> orderColumns = orderBy.getColumnNames();
        List<String> partitionKeyColumns = filteredByKey.getColumnNames();
        List<String> columnsToSelect = new ArrayList<>(entryColumns);
        columnsToSelect.removeAll(partitionKeyColumns);

        QueryFactory.EntryListQueryBuilder queryBuilder = provider.createEntryListQueryBuilder();
        queryBuilder.setTableName(tableName);
        queryBuilder.addSelectedColumns(columnsToSelect);
        queryBuilder.addFilteredByKeyColumns(partitionKeyColumns);
        queryBuilder.addOrderByColumns(orderColumns);
        Map<QuerySlicing.Kind, Query> queries = Stream.of(QuerySlicing.Kind.values())
                .collect(Collectors.toMap(Function.identity(), kind -> {
                    queryBuilder.setQuerySlicingKind(kind);
                    return queryBuilder.build();
                }));
        return (Transaction transaction) -> {
            return (K partitionKeyValue, QuerySlicing<O> slicing) -> {
                QuerySlicing.Kind kind = slicing.kind();
                Query query = Optional.ofNullable(queries.get(slicing.kind()))
                        .orElseThrow(() -> new IllegalStateException("Unprepared slicing " + kind));
                try (PreparedQuery preparedQuery = transaction.prepareQuery(query)) {
                    preparedQuery.fillIn(filteredByKey, partitionKeyValue);
                    if (kind.shouldHaveCondition()) {
                        preparedQuery.fillIn(orderBy, slicing.getComparisonValue());
                    }
                    if (kind.shouldBeLimited()) {
                        preparedQuery.fillIn(LIMIT_ROW_LAYOUT, slicing.getLimit());
                    }
                    QueryResult.Extractor<E> queryExtractor = preparedQuery.createQueryResultExtractor(entry);
                    queryExtractor.setConstantColumns(filteredByKey, partitionKeyValue);
                    try (QueryResult<E> resultSet = queryExtractor.extractQueryResult()) {
                        List<E> result = new ArrayList<>();
                        while (resultSet.next()) {
                            result.add(resultSet.get());
                        }
                        if (kind.shouldBeOrdered() && kind.orderIsReversed())
                            Collections.reverse(result);
                        return result;
                    }
                } catch (SQLTransientException ex) {
                    throw new SQLTransientTransactionException(ex);
                } catch (SQLException ex) {
                    throw new SQLRepositoryException(ex);
                }
            };
        };
    }
}
