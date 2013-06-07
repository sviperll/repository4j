Java code-driven ORM
====================

Allows you to write simple Repository-files for existing Java classes.

Installation
============

This library is not currently in maven central so you'll need to install it manually:

    $ cd sviperll-repository
    $ mvn clean && mvn install

    or

    $ mvn install:install-file -Dfile=sviperll-repository-0.1-SNAPSHOT.jar\
                               -DgroupId=com.github.sviperll\
                               -DartifactId=sviperll-repository\
                               -Dversion=0.1-SNAPSHOT\
                               -Dpackaging=jar

To use it in your projects add this to your pom.xml:

        <dependency>
            <groupId>com.github.sviperll</groupId>
            <artifactId>sviperll-repository</artifactId>
            <version>0.1-SNAPSHOT</version>
        </dependency>

Quick start
===========

Suppose that you have classes

```Java
    public class User {
        private final Credentials credentials;
        private final Contacts contacts;

        public User(Credentials credentials, Contacts contacts) {
            this.credentials = credentials;
            this.contacts = contacts;
        }

        public Credentials credentials() {
            return credentials;
        }

        public Contacts contacts() {
            return contacts;
        }
    }

    public class Credentials {
        private final String login;
        private final String passwordHash;

        public Credentials(String login, String passwordHash) {
            this.login = login;
            this.passwordHash = passwordHash;
        }

        public String login() {
            return login;
        }

        public String passwordHash() {
            return passwordHash;
        }

        public void checkPassword(String password) {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }

    public class Contacts {
        private final String email;
        private final String phone;

        public Contacts(String email, String phone) {
            this.email = email;
            this.phone = phone;
        }

        public String email() {
            return email;
        }

        public String phone() {
            return phone;
        }
    }
```

And you'd like to store it in SQL table

```SQL
    CREATE TABLE users
    ( id INTEGER NOT NULL AUTO_GENERATED
    , login VARCHAR(24) NOT NULL
    , password_hash VARCHAR(64) NOT NULL
    , email VARCHAR(128) NOT NULL
    , phone VARCHAR(24) NOT NULL

    , PRIMARY KEY (id)
    , UNIQUE (login)
    );
```

You can create Repository class to access persistant data and use it like shown below.

```Java
    Connection connection = DriverManager.getConnection("jdbc:...");
    RepositorySupport repositorySupport = new RepositorySupport(connection);
    RepositoryFactory repositoryFactory = new RepositoryFactory(repositorySupport);
    UserRepository repository = new UserRepository(repositoryFactory);

    User guestUser = ...
    UserKey currentUserKey = ...
    User user = repository.get(currentUserKey, OptionalVisitors.<User>returnDefault(guestUser));
    UserEntry adminUserEntry = repository.get("admin", OptionalVisitors.<UserEntry>throwNoSuchElementException());

    List<UserEntry> entries = repository.entryList(RepositorySlicings.<UserKey>firstN(100));
```

There are some generic-signatures inconviniances. But they should go away with
Java 7 and Java 8 type-inference. For instance you should probably write

    OptionalVisitors.<>returnDefault(guestUser)

instead of

    OptionalVisitors.<User>returnDefault(guestUser)

Below is a full implementation of such repository class.
The code contains LOTS of tedius boilerplate, but it is all mechanical and contains no complex logic.
Actually it can all be generated mecanically from some metadata. Once generated it is convinient
to manually modify this file when your table structure changes.

```Java
    public class UserRepository {
        private static final String TABLE_NAME = "users";

        public static UserRepository createInstance(RepositoryFactory factory) {
            TableColumn<UserKey> keyColumn = TableColumns.isomorphic(TableColumns.integer("id"), new Isomorphism<UserKey, Integer>() {
                @Override
                public Integer forward(UserKey userKey) {
                    return userKey.key;
                }

                @Override
                public UserKey backward(Integer key) {
                    return new UserKey(key);
                }
            });
            TableColumn<String> loginColumn = TableColumns.string("login");
            TableColumn<String> passwordHashColumn = TableColumns.string("password_hash");
            TableColumn<String> emailColumn = TableColumns.string("email");
            TableColumn<String> phoneColumn = TableColumns.string("phone");

            StorableClass<Credentials> credentialsClass = StorableClasses.create(loginColumn, passwordHashColumn, new ClassStrucure2<Credentials, String, String>() {
                @Override
                public String getField1(Credentials credentials) {
                    return credentials.login();
                }

                @Override
                public String getField2(Credentials credentials) {
                    return credentials.passwordHash();
                }

                @Override
                public Credentials createInstance(String login, String passwordHash) {
                    return new Credentials(login, passwordHash);
                }
            });

            StorableClass<Contacts> contactsClass = StorableClasses.create(emailColumn, phoneColumn, new ClassStrucure2<Contacts, String, String>() {
                @Override
                public String getField1(Contacts contacts) {
                    return contacts.email();
                }

                @Override
                public String getField2(Contacts contacts) {
                    return contacts.phone();
                }

                @Override
                public Contacts createInstance(String email, String phone) {
                    return new Contacts(email, phone);
                }
            });

            StorableClass<User> userClass = StorableClasses.create(credentialsClass, contactsClass, new ClassStrucure2<User, Credentials, Contacts>() {
                @Override
                public Credentials getField1(User user) {
                    return user.credentials();
                }

                @Override
                public Contacts getField2(User user) {
                    return user.contacts();
                }

                @Override
                public User createInstance(Credentials credentials, Contacts contacts) {
                    return new User(credentials, contacts);
                }
            });


            StorableClass<UserKey> keyClass = StorableClasses.create(keyColumn);

            /*
             * Autogenerated key differs from plain key only by it's retrieval method.
             * keyClass value is retrieved from ResultSet by column name.
             * autogeneratedKeyClass value is retrieved from ResultSet by it's index (1).
             * autogeneratedKeyClass is used to retrieve key value from ResultSet of autogenerated values.
             * (see Statement#getGeneratedKeys())
             */
            StorableClass<UserKey> autogeneratedKeyClass = StorableClasses.create(TableColumns.retrievedByIndex(keyColumn, 1));

            StorableClass<UserEntry> entryClass = StorableClasses.create(keyClass, userClass, new ClassStructure2<UserEntry, UserKey, User>() {
                @Override
                public UserKey getField1(UserEntry entry) {
                    return entry.key();
                }

                @Override
                public User getField2(UserEntry entry) {
                    return entry.attributes();
                }

                @Override
                public UserEntry createInstance(UserKey key, User attributes) {
                    return new UserEntry(key, attributes);
                }
            });

            ReadableRepository<UserEntry, UserKey> readableRepository = factory.readable(TABLE_NAME, entryClass, keyClass);
            AutogeneratedKeyIndexedRepository<UserKey, User> indexedRepository = factory.autogeneratedKeyIndexed(TABLE_NAME, keyClass, autogeneratedKeyClass, userClass);
            IndexedRepository<String, UserEntry> loginIndexedRepository = factory.indexed(TABLE_NAME, StorableClasses.create(loginColumn), entryClass);
            return new UserRepository(readableRepository, indexedRepository, loginIndexedRepository);
        }

        private final ReadableRepository<UserEntry, UserKey> readableRepository;
        private final AutogeneratedKeyIndexedRepository<UserKey, User> indexedRepository;

        private UserRepository(ReadableRepository<UserEntry, UserKey> readableRepository, AutogeneratedKeyIndexedRepository<UserKey, User> indexedRepository, IndexedRepository<String, UserEntry> loginIndexedRepository) {
            this.readableRepository = readableRepository;
            this.indexedRepository = indexedRepository;
            this.loginIndexedRepository = loginIndexedRepository;
        }

        public <R, E extends Exception> R get(UserKey key, OptionalVisitor<User, R, E> optionalVisitor) throws E, SQLException {
            return indexedRepository.get(key, optionalVisitor);
        }

        public <R, E extends Exception> R get(String login, OptionalVisitor<UserEntry, R, E> optionalVisitor) throws E, SQLException {
            return loginIndexedRepository.get(login, optionalVisitor);
        }

        public boolean remove(UserKey key) throws SQLException {
            return indexedRepository.remove(key);
        }

        public UserKey put(User user) throws SQLException {
            return indexedRepository.putNewEntry(user);
        }

        public boolean putIfExists(UserKey key, Changed<User> user) throws SQLException {
            return indexedRepository.putIfExists(key, user);
        }

        public List<UserEntry> entryList(RepositorySlicing<UserKey> slicing) throws SQLException {
            return readableRepository.entryList(slicing);
        }

        public class UserKey implements Comparable<UserKey> {
            private final int key;

            private UserKey(int key) {
                this.key = key;
            }

            @Override
            public compareTo(UserKey that) {
                this == that ? 0 : this.key - that.key;
            }

            @Override
            public boolean equals(Object thatObject) {
                if (this == thatObject)
                    return true;
                else if (!(thatObject instaceof UserKey))
                    return false;
                else {
                    UserKey that = (UserKey)thatObject;
                    return this.key == that.key;
                }
            }
        }

        public class UserEntry {
            private final UserKey key;
            private final User attributes;
            public UserEntry(UserKey key, User attributes) {
                this.key = key;
                this.attributes = attributes;
            }

            public UserKey key() {
                return key;
            }

            public User attributes() {
                return attributes;
            }
        }
    }
```