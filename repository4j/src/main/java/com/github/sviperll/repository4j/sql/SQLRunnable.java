package com.github.sviperll.repository4j.sql;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLRunnable {
    void run() throws SQLException;
}
