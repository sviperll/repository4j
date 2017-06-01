package com.github.sviperll.repository4j.example;

import com.github.sviperll.repository4j.QuerySlicing;
import com.github.sviperll.repository4j.RepositoryException;
import com.github.sviperll.repository4j.TransientTransactionException;

import java.util.List;
import java.util.Optional;

interface UserRepository {
    Optional<User> getByID(long id) throws TransientTransactionException, RepositoryException;

    Optional<User> getByLogin(String login) throws TransientTransactionException, RepositoryException;

    List<User> entryList(QuerySlicing<User> slicing) throws TransientTransactionException, RepositoryException;

    User put(Credentials credentials) throws TransientTransactionException, RepositoryException;
}
