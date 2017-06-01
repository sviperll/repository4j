package com.github.sviperll.repository4j;

import com.github.sviperll.repository4j.jdbcwrapper.Query;

import java.util.List;
import java.util.Map;

public interface QueryFactory {
    Query getQuery(String tableName, List<String> keyColumns, List<String> selectedColumns);

    EntryListQueryBuilder createEntryListQueryBuilder();

    Query putQuery(String tableName, List<String> valueColumns, Map<String, String> columnExpressions);

    interface EntryListQueryBuilder {
        void setTableName(String tableName);
        void addSelectedColumns(List<String> columnNames);
        void addFilteredByKeyColumns(List<String> columnNames);
        void addOrderByColumns(List<String> columnNames);
        void setQuerySlicingKind(QuerySlicing.Kind querySlicingkind);
        Query build();
    }
}
