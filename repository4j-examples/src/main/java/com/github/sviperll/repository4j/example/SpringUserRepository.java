package com.github.sviperll.repository4j.example;

import com.github.sviperll.repository4j.RepositoryException;
import com.github.sviperll.repository4j.RepositoryFunction;
import com.github.sviperll.repository4j.SQLDisconnectedRepositoryFunction;
import com.github.sviperll.repository4j.SQLRepositoryException;
import com.github.sviperll.repository4j.TransientTransactionException;
import com.github.sviperll.repository4j.jdbcwrapper.Transaction;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;

class SpringUserRepository extends AbstractUserRepository {
    private final JdbcTemplate springJdbcTemplate;

    SpringUserRepository(JdbcTemplate springJdbcTemplate) {
        this.springJdbcTemplate = springJdbcTemplate;
    }

    @Override
    protected <T, R> RepositoryFunction<T, R> getRepositoryFunction(SQLDisconnectedRepositoryFunction<T, R> disconnectedFunction) {
        return (T argument) -> {
            try {
                return springJdbcTemplate.execute((Connection connection) -> {
                    try {
                        return disconnectedFunction.getRepositoryFunction(Transaction.getCurrent(connection))
                                .apply(argument);
                    } catch (RepositoryException|TransientTransactionException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            } catch (BadSqlGrammarException ex) {
                throw new SQLRepositoryException(ex.getSQLException());
            } catch (RuntimeException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof RepositoryException)
                    throw (RepositoryException)cause;
                else if (cause instanceof TransientTransactionException)
                    throw (TransientTransactionException)cause;
                else
                    throw ex;
            }
        };
    }
}
