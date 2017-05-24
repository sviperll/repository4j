package com.github.sviperll.repository4j.example;

class PasswordHash {
    private final String hash;

    static PasswordHash hashPassword(String password) {
        throw new UnsupportedOperationException();
    }
    static PasswordHash fromPrecomputedHashValue(String hash) {
        return new PasswordHash(hash);
    }

    private PasswordHash(String hash) {
        this.hash = hash;
    }

    boolean isCorrectPassword(String password) {
        PasswordHash test = hashPassword(password);
        return test.hash.equals(this.hash);
    }

    String hashValue() {
        return hash;
    }
}
