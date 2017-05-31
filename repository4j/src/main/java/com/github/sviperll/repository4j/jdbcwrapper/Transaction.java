package com.github.sviperll.repository4j.jdbcwrapper;

import com.github.sviperll.repository4j.sql.SQLRunnable;

import java.sql.Connection;
import java.sql.SQLException;

public class Transaction implements AutoCloseable {
    public static Transaction getCurrent(Connection connection) {
        return new Transaction(connection);
    }

    private final Connection connection;
    private boolean isCommited = false;

    Transaction(Connection connection) {
        this.connection = connection;
    }

    public PreparedQuery prepareQuery(Query query) throws SQLException {
        if (isCommited)
            throw new IllegalStateException("Transaction is already commited");
        try {
            return PreparedQuery.createInstance(query.placeholders(), connection.prepareStatement(query.toString()));
        } catch (SQLException e) {
            throw SQLExceptions.precise(e);
        }
    }

    public void commit() throws SQLException {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw SQLExceptions.precise(e);
        }
        isCommited = true;
    }

    @Override
    public void close() throws SQLException {
        if (!isCommited) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw SQLExceptions.precise(e);
            }
            isCommited = true;
        }
    }

    public enum Isolation {
        NONE(Connection.TRANSACTION_NONE),
        READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
        READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
        REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
        SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

        private final int jdbcCode;

        Isolation(int jdbcCode) {
            this.jdbcCode = jdbcCode;
        }

        SQLRunnable createJdbcConnectionConfigurer(Connection connection) {
            return () -> {
                connection.setTransactionIsolation(jdbcCode);
            };
        }
    }
}
