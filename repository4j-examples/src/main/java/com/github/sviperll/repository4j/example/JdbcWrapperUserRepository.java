package com.github.sviperll.repository4j.example;

import com.github.sviperll.repository4j.QuerySlicing;
import com.github.sviperll.repository4j.RepositoryException;
import com.github.sviperll.repository4j.RepositoryFunction;
import com.github.sviperll.repository4j.TransientTransactionException;
import com.github.sviperll.repository4j.jdbcwrapper.Transaction;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

class JdbcWrapperUserRepository extends UserMapping {

    private final RepositoryFunction<Long, Optional<User>> getById;
    private final RepositoryFunction<String, Optional<User>> getByLogin;
    private final RepositoryFunction<QuerySlicing<String>, List<User>> entryList;

    JdbcWrapperUserRepository(Transaction transaction) throws SQLException {
        this.getById = GET_BY_ID.getRepositoryFunction(transaction);
        this.getByLogin = GET_BY_LOGIN.getRepositoryFunction(transaction);
        this.entryList = ENTRY_LIST.getRepositoryFunction(transaction);
    }

    Optional<User> getByID(long id) throws TransientTransactionException, RepositoryException {
        return getById.apply(id);
    }

    Optional<User> getByLogin(String login) throws TransientTransactionException, RepositoryException {
        return getByLogin.apply(login);
    }

    List<User> entryList(QuerySlicing<User> slicing) throws TransientTransactionException, RepositoryException {
        return entryList.apply(slicing.map(User::login));
    }
}
