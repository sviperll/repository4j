package com.github.sviperll.repository4j.example;

import com.github.sviperll.repository4j.SQLDisconnectedRepositoryFunction;
import com.github.sviperll.repository4j.QuerySlicing;
import com.github.sviperll.repository4j.SQLRepositoryMethodFactory;
import com.github.sviperll.repository4j.jdbcwrapper.rawlayout.ConstructorRowLayoutBuilder;
import com.github.sviperll.repository4j.jdbcwrapper.rawlayout.RowLayout;
import com.github.sviperll.repository4j.Oracle;

import java.util.List;
import java.util.Optional;

class UserMapping {
    protected static final String TABLE_NAME = "users";
    protected static final RowLayout<Long> ID_COLUMN = RowLayout.longColumn("id");
    protected static final RowLayout<String> LOGIN_COLUMN = RowLayout.stringColumn("login");
    protected static final RowLayout<PasswordHash> PASSWORD_HASH_COLUMN = RowLayout.stringColumn("password_hash")
            .morph(PasswordHash::fromPrecomputedHashValue, PasswordHash::hashValue);

    protected static final RowLayout<User> USER_LAYOUT;
    protected static final SQLDisconnectedRepositoryFunction<Long, Optional<User>> GET_BY_ID;
    protected static final SQLDisconnectedRepositoryFunction<String, Optional<User>> GET_BY_LOGIN;
    protected static final SQLDisconnectedRepositoryFunction<QuerySlicing<String>, List<User>> ENTRY_LIST;

    static {
        ConstructorRowLayoutBuilder<Credentials, String, PasswordHash> credentialsBuilder =
                RowLayout.forConstructor(Credentials::new);
        credentialsBuilder.setComponent1(LOGIN_COLUMN, Credentials::login);
        credentialsBuilder.setComponent2(PASSWORD_HASH_COLUMN, Credentials::passwordHash);
        RowLayout<Credentials> credentials = credentialsBuilder.build();

        ConstructorRowLayoutBuilder<User, Long, Credentials> userBuilder = RowLayout.forConstructor(User::new);
        userBuilder.setComponent1(ID_COLUMN, User::id);
        userBuilder.setComponent2(credentials, User::credentials);
        USER_LAYOUT = userBuilder.build();

        SQLRepositoryMethodFactory methodFactory = new SQLRepositoryMethodFactory(Oracle.getInstance());
        GET_BY_ID = methodFactory.get(TABLE_NAME, ID_COLUMN, USER_LAYOUT);
        GET_BY_LOGIN = methodFactory.get(TABLE_NAME, LOGIN_COLUMN, USER_LAYOUT);
        ENTRY_LIST = methodFactory.entryList(TABLE_NAME, USER_LAYOUT, LOGIN_COLUMN);
    }
}
