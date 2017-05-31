package com.github.sviperll.repository4j;

import java.sql.SQLException;

/**
 * Means that transaction may and probably should be retried.
 */
public class TransientTransactionException extends Exception {
    public TransientTransactionException(Exception cause) {
        super(cause);
    }
}
