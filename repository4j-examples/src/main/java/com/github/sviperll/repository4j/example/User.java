package com.github.sviperll.repository4j.example;

class User {
    private final long id;
    private final Credentials credentials;

    User(long id, Credentials credentials) {
        this.id = id;
        this.credentials = credentials;
    }

    long id() {
        return id;
    }

    Credentials credentials() {
        return credentials;
    }

    String login() {
        return credentials.login();
    }
}
