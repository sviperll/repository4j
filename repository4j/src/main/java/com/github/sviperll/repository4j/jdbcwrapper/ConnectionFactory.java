package com.github.sviperll.repository4j.jdbcwrapper;

import com.github.sviperll.repository4j.sql.SQLRunnable;
import com.github.sviperll.repository4j.sql.SQLSupplier;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionFactory {
    private final SQLSupplier<Connection> jdbcConnectionFactory;
    private final Transaction.Isolation isolation;

    /** Use SERIALIZABLE transaction isolation by default. */
    public ConnectionFactory(SQLSupplier<Connection> jdbcConnectionFactory) {
        this(jdbcConnectionFactory, Transaction.Isolation.SERIALIZABLE);
    }

    public ConnectionFactory(SQLSupplier<Connection> jdbcConnectionFactory, Transaction.Isolation isolation) {
        this.jdbcConnectionFactory = jdbcConnectionFactory;
        this.isolation = isolation;
    }

    public QueriableConnection openConnection() throws SQLException {
        Connection jdbcConnection = jdbcConnectionFactory.get();
        jdbcConnection.setAutoCommit(false);
        SQLRunnable configurer = isolation.createJdbcConnectionConfigurer(jdbcConnection);
        configurer.run();
        return new QueriableConnection(jdbcConnection);
    }
}
