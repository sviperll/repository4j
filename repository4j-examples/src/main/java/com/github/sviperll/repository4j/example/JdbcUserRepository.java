package com.github.sviperll.repository4j.example;

import com.github.sviperll.repository4j.RepositoryFunction;
import com.github.sviperll.repository4j.SQLDisconnectedRepositoryFunction;
import com.github.sviperll.repository4j.jdbcwrapper.Transaction;

import java.sql.Connection;
import java.sql.SQLException;

class JdbcUserRepository extends AbstractUserRepository {

    private final Connection connection;

    JdbcUserRepository(Connection connection) throws SQLException {
        this.connection = connection;
    }

    @Override
    protected <T, R> RepositoryFunction<T, R> getRepositoryFunction(SQLDisconnectedRepositoryFunction<T, R> disconnectedFunction) {
        return disconnectedFunction.getRepositoryFunction(Transaction.getCurrent(connection));
    }
}
