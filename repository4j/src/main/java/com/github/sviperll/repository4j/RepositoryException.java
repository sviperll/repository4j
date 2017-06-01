package com.github.sviperll.repository4j;

import java.sql.SQLException;

public class RepositoryException extends Exception {
    public RepositoryException(String message) {
        super(message);
    }
    public RepositoryException(Exception cause) {
        super(cause);
    }
}
