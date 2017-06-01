package com.github.sviperll.repository4j;

import com.github.sviperll.repository4j.jdbcwrapper.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OracleQueryFactory implements QueryFactory {
    private static final OracleQueryFactory INSTANCE = new OracleQueryFactory();

    public static OracleQueryFactory getInstance() {
        return INSTANCE;
    }

    private OracleQueryFactory() {
    }

    @Override
    public Query getQuery(String tableName, List<String> keyColumns, List<String> columnsToSelect) {
        return Query.compile(
                "SELECT * FROM (SELECT " + columnsToSelect.stream().collect(Collectors.joining(", "))
                        + " FROM " + tableName
                        + " WHERE " + QueryFactories.applyOperator(keyColumns, "=", " AND ") + ") q WHERE ROWNUM <= 1");
    }

    @Override
    public EntryListQueryBuilder createEntryListQueryBuilder() {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Query putQuery(String tableName, List<String> valueColumns, Map<String, String> columnExpressions) {
        List<String> expressionColumns = new ArrayList<>(columnExpressions.keySet());
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("INSERT INTO ").append(tableName);
        queryBuilder.append(" (");
        queryBuilder.append(
                Stream.concat(expressionColumns.stream(), valueColumns.stream()).collect(Collectors.joining(", ")));
        queryBuilder.append(") VALUES (");
        queryBuilder.append(
                Stream.concat(expressionColumns.stream().map(c -> columnExpressions.get(c)),
                        valueColumns.stream().map(c -> ":" + c)).collect(Collectors.joining(", ")));
        queryBuilder.append(")");
        return Query.compile(queryBuilder.toString());
    }

    private static class OracleEntryListQueryBuilder implements EntryListQueryBuilder {
        private String tableName;
        private List<String> entryColumns = new ArrayList<>();
        private List<String> partitionKeyColumns = new ArrayList<>();
        private List<String> orderColumns = new ArrayList<>();
        private QuerySlicing.Kind kind;

        @Override
        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        @Override
        public void addSelectedColumns(List<String> columnNames) {
            this.entryColumns.addAll(columnNames);
        }

        @Override
        public void addFilteredByKeyColumns(List<String> columnNames) {
            this.partitionKeyColumns.addAll(columnNames);
        }

        @Override
        public void addOrderByColumns(List<String> columnNames) {
            this.orderColumns.addAll(columnNames);
        }

        @Override
        public void setQuerySlicingKind(QuerySlicing.Kind querySlicingKind) {
            this.kind = querySlicingKind;
        }

        @Override
        public Query build() {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT ").append(entryColumns.stream().collect(Collectors.joining(", ")));
            queryBuilder.append(" FROM ").append(tableName);
            if (kind.shouldHaveCondition() || !partitionKeyColumns.isEmpty()) {
                queryBuilder.append(" WHERE ");
                if (!partitionKeyColumns.isEmpty()) {
                    queryBuilder.append(QueryFactories.applyOperator(partitionKeyColumns, "=", " AND "));
                }
                if (kind.shouldHaveCondition()) {
                    String operator = kind.conditionIsGreater() ? ">" : "<";
                    if (partitionKeyColumns.isEmpty()) {
                        queryBuilder.append(QueryFactories.lexComparison(orderColumns, operator));
                    } else {
                        queryBuilder.append(" AND (");
                        queryBuilder.append(QueryFactories.lexComparison(orderColumns, operator));
                        queryBuilder.append(")");
                    }
                }
            }
            if (kind.shouldBeOrdered()) {
                queryBuilder.append(" ORDER BY ");
                if (kind.orderIsStraight())
                    queryBuilder.append(orderColumns.stream().collect(Collectors.joining(", ")));
                else
                    queryBuilder.append(orderColumns.stream().map(s -> s + " DESC").collect(Collectors.joining(", ")));
            }
            if (!kind.shouldBeLimited()) {
                return Query.compile(queryBuilder.toString());
            } else {
                return Query.compile("SELECT * FROM ("
                        + queryBuilder.toString()
                        + ") q WHERE ROWNUM <= :" + QueryFactories.LIMIT_COLUMN_NAME);
            }
        }
    }
}
