package com.github.sviperll.repository4j.jdbcwrapper;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLTransientException;

class SQLExceptions {
    static SQLException precise(SQLException ex) {
        if (ex.getSQLState().equals("40001") && !(ex instanceof SQLTransientException)) {
            return new SQLTransientException(ex.getMessage(), ex.getSQLState(), ex.getErrorCode(), ex);
        } else if (ex.getSQLState().startsWith("23") && !(ex instanceof SQLIntegrityConstraintViolationException)) {
            return new SQLIntegrityConstraintViolationException(
                    ex.getMessage(), ex.getSQLState(), ex.getErrorCode(), ex);
        } else {
            return ex;
        }
    }

    private SQLExceptions() {
    }
}
