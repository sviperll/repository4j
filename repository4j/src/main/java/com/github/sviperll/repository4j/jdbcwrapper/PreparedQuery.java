package com.github.sviperll.repository4j.jdbcwrapper;

import com.github.sviperll.repository4j.jdbcwrapper.rawlayout.RowLayout;
import com.github.sviperll.repository4j.sql.SQLConsumer;
import com.github.sviperll.repository4j.sql.WritableRaw;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PreparedQuery implements AutoCloseable {
    static PreparedQuery createInstance(Map<String, List<Integer>> indecies,
                                        PreparedStatement preparedStatement,
                                        boolean hasGeneratedColumns) {
        return new PreparedQuery(new WritablePreparedStatement(indecies, preparedStatement, hasGeneratedColumns));
    }
    private final WritablePreparedStatement preparedStatement;

    private PreparedQuery(WritablePreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    @Override
    public void close() throws SQLException {
        try {
            preparedStatement.close();
        } catch (SQLException e) {
            throw SQLExceptions.precise(e);
        }
    }

    public UpdateResult executeUpdate() throws SQLException {
        try {
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw SQLExceptions.precise(e);
        }
    }

    public <T> QueryResult.Extractor<T> createQueryResultExtractor(RowLayout<T> rowLayout) {
        return preparedStatement.createQueryResultExtractor(rowLayout);
    }

    public <T> void fillIn(RowLayout<T> rowLayout, T value)
            throws SQLException {

        SQLConsumer<T> setter = rowLayout.createRawWriter(preparedStatement);
        try {
            setter.accept(value);
        } catch (SQLException e) {
            throw SQLExceptions.precise(e);
        }
    }

    private static class WritablePreparedStatement implements WritableRaw, AutoCloseable {
        private final Map<String, List<Integer>> indecies;
        private final PreparedStatement preparedStatement;
        private final boolean hasGeneratedColumns;

        WritablePreparedStatement(Map<String, List<Integer>> indecies,
                                  PreparedStatement statement,
                                  boolean hasGeneratedColumns) {
            this.indecies = indecies;
            this.preparedStatement = statement;
            this.hasGeneratedColumns = hasGeneratedColumns;
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

        <T> QueryResult.Extractor<T> createQueryResultExtractor(RowLayout<T> rowLayout) {
            return new QueryResult.Extractor<>(rowLayout, this::executeQuery);
        }

        private ReadableResult executeQuery() throws SQLException {
            return new ReadableResult.OfResultSet(preparedStatement.executeQuery());
        }

        UpdateResult executeUpdate() throws SQLException {
            int count = preparedStatement.executeUpdate();
            if (hasGeneratedColumns) {
                return new UpdateResult(count, () -> getGeneratedKeys(count));
            } else {
                return new UpdateResult(count, () -> new ReadableResult.EmptyRows(count));
            }
        }

        private ReadableResult getGeneratedKeys(int count) throws SQLException {
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            return generatedKeys == null
                    ? new ReadableResult.EmptyRows(count)
                    : new ReadableResult.OfResultSet(generatedKeys);
        }
    }
}
