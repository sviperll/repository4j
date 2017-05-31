package com.github.sviperll.repository4j.example;

import com.github.sviperll.repository4j.QuerySlicing;
import com.github.sviperll.repository4j.RepositoryException;
import com.github.sviperll.repository4j.RepositoryFunction;
import com.github.sviperll.repository4j.SQLDisconnectedRepositoryFunction;
import com.github.sviperll.repository4j.TransientTransactionException;

import java.util.List;
import java.util.Optional;

abstract class AbstractUserRepository extends UserMapping implements UserRepository {

    protected abstract <T, R> RepositoryFunction<T, R> getRepositoryFunction(SQLDisconnectedRepositoryFunction<T, R> disconnectedFunction);

    @Override
    public Optional<User> getByID(long id) throws TransientTransactionException, RepositoryException {
        return getRepositoryFunction(GET_BY_ID).apply(id);
    }

    @Override
    public Optional<User> getByLogin(String login) throws TransientTransactionException, RepositoryException {
        return getRepositoryFunction(GET_BY_LOGIN).apply(login);
    }

    @Override
    public List<User> entryList(QuerySlicing<User> slicing) throws TransientTransactionException, RepositoryException {
        return getRepositoryFunction(ENTRY_LIST).apply(slicing.map(User::login));
    }
}
