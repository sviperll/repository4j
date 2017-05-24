package com.github.sviperll.repository4j.provider;

import com.github.sviperll.repository4j.jdbcwrapper.Query;
import com.github.sviperll.repository4j.QuerySlicing;

import java.util.List;
import java.util.stream.Collectors;

public class Oracle implements SQLProvider {
    private static final Oracle INSTANCE = new Oracle();

    public static Oracle getInstance() {
        return INSTANCE;
    }

    private Oracle() {
    }

    @Override
    public Query get(String tableName, List<String> keyColumns, List<String> columnsToSelect) {
        return Query.compile(
                "SELECT * FROM (SELECT " + columnsToSelect.stream().collect(Collectors.joining(", "))
                        + " FROM " + tableName
                        + " WHERE " + SQLHelper.applyOperator(keyColumns, "=", " AND ") + ") q WHERE ROWNUM <= 1");
    }

    @Override
    public Query entryList(String tableName,
                           List<String> entryColumns,
                           List<String> partitionKeyColumns,
                           List<String> orderColumns,
                           QuerySlicing.Kind kind) {

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ").append(entryColumns.stream().collect(Collectors.joining(", ")));
        queryBuilder.append(" FROM ").append(tableName);
        if (kind.shouldHaveCondition() || !partitionKeyColumns.isEmpty()) {
            queryBuilder.append(" WHERE ");
            if (!partitionKeyColumns.isEmpty()) {
                queryBuilder.append(SQLHelper.applyOperator(partitionKeyColumns, "=", " AND "));
            }
            if (kind.shouldHaveCondition()) {
                String operator = kind.conditionIsGreater() ? ">" : "<";
                if (partitionKeyColumns.isEmpty()) {
                    queryBuilder.append(SQLHelper.lexComparison(orderColumns, operator));
                } else {
                    queryBuilder.append(" AND (");
                    queryBuilder.append(SQLHelper.lexComparison(orderColumns, operator));
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
                    + ") q WHERE ROWNUM <= :" + SQLHelper.LIMIT_COLUMN_NAME);
        }
    }
}
