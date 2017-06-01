package com.github.sviperll.repository4j.jdbcwrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Query {

    /**
     * Compiles query text with denoted placeholders into Query-object.
     *
     * Example query:
     * <p>
     * <tt>{@code SELECT id, name FROM users WHERE login = :login}</tt>
     * <p>
     * Placeholder is a text formed of a colon and a valid Java-identifier.
     */

    public static Query compile(String queryString) {
        CompileState state = CompileState.IN_TEXT;
        Builder builder = new Builder();
        StringBuilder fragment = new StringBuilder();
        for (char c : queryString.toCharArray()) {
            if (state == CompileState.IN_TEXT) {
                if (c != ':') {
                    fragment.append(c);
                } else {
                    state = CompileState.FOUND_COLON;
                }
            } else if (state == CompileState.FOUND_COLON) {
                if (Character.isJavaIdentifierStart(c)) {
                    builder.appendPlainSQL(fragment.toString());
                    fragment = new StringBuilder();
                    state = CompileState.IN_PLACEHOLDER;
                } else {
                    fragment.append(':');
                    state = CompileState.IN_TEXT;
                }
                fragment.append(c);
            } else if (state == CompileState.IN_PLACEHOLDER) {
                if (!Character.isJavaIdentifierPart(c)) {
                    builder.appendPlaceholder(fragment.toString());
                    fragment = new StringBuilder();
                    state = CompileState.IN_TEXT;
                }
                fragment.append(c);
            }
        }
        return builder.build();
    }

    private final String text;
    private final Map<String, List<Integer>> placeholders;

    private Query(String text, Map<String, List<Integer>> placeholders) {
        this.text = text;
        this.placeholders = Collections.unmodifiableMap(new TreeMap<>(placeholders));
    }

    @Override
    public String toString() {
        return text;
    }

    Map<String, List<Integer>> placeholders() {
        return placeholders;
    }

    public static class Builder {
        private final StringBuilder builder = new StringBuilder();
        private final Map<String, List<Integer>> placeholders = new TreeMap<>();
        private int numberOfPlaceholders = 0;

        public Builder() {
        }

        public void appendPlainSQL(String fragment) {
            builder.append(fragment);
        }

        public void appendPlaceholder(String columnName) {
            builder.append("?");
            numberOfPlaceholders++;
            int index = numberOfPlaceholders;
            List<Integer> indecies = placeholders.get(columnName);
            if (indecies == null) {
                indecies = new ArrayList<>();
                placeholders.put(columnName, indecies);
            }
            indecies.add(index);
        }

        public Query build() {
            return new Query(builder.toString(), placeholders);
        }

    }

    private enum CompileState {
        IN_TEXT, IN_PLACEHOLDER, FOUND_COLON
    }

}
