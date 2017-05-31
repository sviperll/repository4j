package com.github.sviperll.repository4j;

import java.sql.SQLException;

public class SQLRepositoryException extends RepositoryException {
    public SQLRepositoryException(SQLException cause) {
        super(cause);
    }

    @Override
    @SuppressWarnings("unchecked")
    public SQLException getCause() {
        return (SQLException)super.getCause();
    }
}
