package com.github.sviperll.repository4j;

import java.sql.SQLException;

public class SQLRepositoryException extends RepositoryException {
    public SQLRepositoryException(String message) {
        super(message);
    }

    public SQLRepositoryException(SQLException cause) {
        super(cause);
    }

    @Override
    @SuppressWarnings("unchecked")
    public SQLException getCause() {
        return (SQLException)super.getCause();
    }
}
