package com.github.sviperll.repository4j;

import java.sql.SQLTransientException;

public class SQLTransientTransactionException extends TransientTransactionException {
    public SQLTransientTransactionException(SQLTransientException cause) {
        super(cause);
    }

    @Override
    @SuppressWarnings("unchecked")
    public SQLTransientException getCause() {
        return (SQLTransientException)super.getCause();
    }
}
