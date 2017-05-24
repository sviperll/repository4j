package com.github.sviperll.repository4j.provider;

import com.github.sviperll.repository4j.jdbcwrapper.Query;
import com.github.sviperll.repository4j.QuerySlicing;

import java.util.List;

public interface SQLProvider {
    Query get(String tableName, List<String> keyColumns, List<String> columnsToSelect);

    /**
     *
     * @param tableName
     * @param entryColumns shouldn't be empty
     * @param partitionKeyColumns CAN BE EMPTY
     * @param orderColumns shouldn't be empty
     * @param kind kind of slicing performed
     * @return
     */
    Query entryList(String tableName,
                    List<String> entryColumns,
                    List<String> partitionKeyColumns,
                    List<String> orderColumns,
                    QuerySlicing.Kind kind);
}
