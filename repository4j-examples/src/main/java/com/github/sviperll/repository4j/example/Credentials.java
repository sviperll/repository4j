package com.github.sviperll.repository4j.example;

class Credentials {
    private final String login;
    private final PasswordHash passwordHash;

    Credentials(String login, PasswordHash passwordHash) {

        this.login = login;
        this.passwordHash = passwordHash;
    }

    String login() {
        return login;
    }

    PasswordHash passwordHash() {
        return passwordHash;
    }
}
