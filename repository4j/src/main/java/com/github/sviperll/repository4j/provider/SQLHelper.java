package com.github.sviperll.repository4j.provider;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SQLHelper {
    public static final String LIMIT_COLUMN_NAME = "$$limit";

    static String lexComparison(List<String> orderColumns, String operator) {
        return IntStream.range(0, orderColumns.size())
                .mapToObj(i -> lexComparisonComponent(orderColumns, operator, i))
                .collect(Collectors.joining(" OR "));
    }

    private static String lexComparisonComponent(List<String> orderColumns, String operator, int index) {
        String comparison = applyOperator(orderColumns.get(index), operator);
        List<String> prefixColumns = orderColumns.subList(0, index);
        return prefixColumns.isEmpty()
                ? comparison
                : "(" + applyOperator(prefixColumns, "=", " AND ") + " AND " + comparison + ")";
    }

    static String applyOperator(String columnName, String operator) {
        return columnName + " " + operator + " :" + columnName;
    }

    static String applyOperator(List<String> columnNames, String operator, String delimiter) {
        return columnNames.stream().map(name -> applyOperator(name, operator)).collect(Collectors.joining(delimiter));
    }

    private SQLHelper() {
        throw new UnsupportedOperationException("Shouldn't be called");
    }
}
