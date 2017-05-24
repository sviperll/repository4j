package com.github.sviperll.repository4j.jdbcwrapper;

import com.github.sviperll.repository4j.SQLConsumer;
import com.github.sviperll.repository4j.jdbcwrapper.rawlayout.RowLayout;
import com.github.sviperll.repository4j.jdbcwrapper.rawlayout.WritableRaw;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PreparedQuery implements AutoCloseable {
    static PreparedQuery createInstance(Map<String, List<Integer>> indecies, PreparedStatement preparedStatement) {
        return new PreparedQuery(new WritablePreparedStatement(indecies, preparedStatement));
    }
    private final WritablePreparedStatement preparedStatement;

    private PreparedQuery(WritablePreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    @Override
    public void close() throws SQLException {
        preparedStatement.close();
    }

    public int executeUpdate() throws SQLException {
        return preparedStatement.executeUpdate();
    }

    public <T> Query.Executor<T> createQueryExecutor(RowLayout<T> rowLayout) throws SQLException {
        return preparedStatement.createQueryExecutor(rowLayout);
    }

    public <T> void fillIn(RowLayout<T> rowLayout, T value) throws SQLException {
        SQLConsumer<T> setter = rowLayout.createRawWriter(preparedStatement);
        setter.accept(value);
    }

    private static class WritablePreparedStatement implements WritableRaw, AutoCloseable {
        private final Map<String, List<Integer>> indecies;
        private final PreparedStatement preparedStatement;

        WritablePreparedStatement(Map<String, List<Integer>> indecies, PreparedStatement statement) {
            this.indecies = indecies;
            this.preparedStatement = statement;
        }

        @Override
        public void setNull(String columnName, int sqlType) throws SQLException {
            for (Integer index: indecies.getOrDefault(columnName, Collections.emptyList())) {
                preparedStatement.setNull(index, sqlType);
            }
        }

        @Override
        public void setInt(String columnName, int value) throws SQLException {
            for (Integer index: indecies.getOrDefault(columnName, Collections.emptyList())) {
                preparedStatement.setInt(index, value);
            }
        }

        @Override
        public void setLong(String columnName, long value) throws SQLException {
            for (Integer index: indecies.getOrDefault(columnName, Collections.emptyList())) {
                preparedStatement.setLong(index, value);
            }
        }

        @Override
        public void setString(String columnName, String value) throws SQLException {
            for (Integer index: indecies.getOrDefault(columnName, Collections.emptyList())) {
                preparedStatement.setString(index, value);
            }
        }

        @Override
        public void close() throws SQLException {
            preparedStatement.close();
        }

        <T> Query.Executor<T> createQueryExecutor(RowLayout<T> rowLayout) {
            return new Query.Executor<T>(rowLayout, preparedStatement);
        }

        int executeUpdate() throws SQLException {
            return preparedStatement.executeUpdate();
        }
    }
}
