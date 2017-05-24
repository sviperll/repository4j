package com.github.sviperll.repository4j.jdbcwrapper;

import java.sql.Connection;

public class QueriableConnection {
    private final Connection connection;

    QueriableConnection(Connection connection) {
        this.connection = connection;
    }

    public Transaction openTransaction() {
        return new Transaction(connection);
    }
}
