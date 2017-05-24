package com.github.sviperll.repository4j.example;

import com.github.sviperll.repository4j.SQLFunction;
import com.github.sviperll.repository4j.DisconnectedSQLFunction;
import com.github.sviperll.repository4j.QuerySlicing;
import com.github.sviperll.repository4j.RepositoryMethodFactory;
import com.github.sviperll.repository4j.jdbcwrapper.Transaction;
import com.github.sviperll.repository4j.jdbcwrapper.rawlayout.ConstructorRowLayoutBuilder;
import com.github.sviperll.repository4j.jdbcwrapper.rawlayout.RowLayout;
import com.github.sviperll.repository4j.provider.Oracle;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

class UserRepository {
    private static final DisconnectedSQLFunction<Long, Optional<User>> DISCONNECTED_GET_BY_ID;
    private static final DisconnectedSQLFunction<String, Optional<User>> DISCONNECTED_GET_BY_LOGIN;
    private static final DisconnectedSQLFunction<QuerySlicing<String>, List<User>> DISCONNECTED_ENTRY_LIST;
    static {
        String tableName = "users";
        RowLayout<Long> idColumn = RowLayout.longColumn("id");
        RowLayout<String> loginColumn = RowLayout.stringColumn("login");
        RowLayout<PasswordHash> passwordHashColumn = RowLayout.stringColumn("password_hash")
                .morph(PasswordHash::fromPrecomputedHashValue, PasswordHash::hashValue);

        ConstructorRowLayoutBuilder<Credentials, String, PasswordHash> credentialsBuilder =
                RowLayout.forConstructor(Credentials::new);
        credentialsBuilder.setComponent1(loginColumn, Credentials::login);
        credentialsBuilder.setComponent2(passwordHashColumn, Credentials::passwordHash);
        RowLayout<Credentials> credentials = credentialsBuilder.build();

        ConstructorRowLayoutBuilder<User, Long, Credentials> userBuilder = RowLayout.forConstructor(User::new);
        userBuilder.setComponent1(idColumn, User::id);
        userBuilder.setComponent2(credentials, User::credentials);
        RowLayout<User> user = userBuilder.build();

        RepositoryMethodFactory methodFactory = new RepositoryMethodFactory(Oracle.getInstance());
        DISCONNECTED_GET_BY_ID = methodFactory.get(tableName, idColumn, user);
        DISCONNECTED_GET_BY_LOGIN = methodFactory.get(tableName, loginColumn, user);
        DISCONNECTED_ENTRY_LIST = methodFactory.entryList(tableName, user, loginColumn);
    }

    private final SQLFunction<Long, Optional<User>> getById;
    private final SQLFunction<String, Optional<User>> getByLogin;
    private final SQLFunction<QuerySlicing<String>, List<User>> entryList;

    UserRepository(Transaction transaction) throws SQLException {
        this.getById = DISCONNECTED_GET_BY_ID.getSQLFunction(transaction);
        this.getByLogin = DISCONNECTED_GET_BY_LOGIN.getSQLFunction(transaction);
        this.entryList = DISCONNECTED_ENTRY_LIST.getSQLFunction(transaction);
    }

    Optional<User> getByID(long id) throws SQLException {
        return getById.apply(id);
    }

    Optional<User> getByLogin(String login) throws SQLException {
        return getByLogin.apply(login);
    }

    List<User> entryList(QuerySlicing<User> slicing) throws SQLException {
        return entryList.apply(slicing.map(User::login));
    }
}
