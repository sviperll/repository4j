package com.github.sviperll.repository4j;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;

public abstract class QuerySlicing<T> {

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final NoSlicing NO_SLICING = new NoSlicing();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> QuerySlicing<T> all() {
        return NO_SLICING;
    }

    public static <T> QuerySlicing<T> firstN(int limit) {
        return new QuerySlicing<T>() {
            @Override
            public <R> R accept(Visitor<T, R> visitor) {
                return visitor.firstN(limit);
            }
        };
    }

    public static <T> QuerySlicing<T> lastN(int limit) {
        return new QuerySlicing<T>() {
            @Override
            public <R> R accept(Visitor<T, R> visitor) {
                return visitor.lastN(limit);
            }
        };
    }

    public static <T> QuerySlicing<T> firstNAfter(T key, int limit) {
        return new QuerySlicing<T>() {
            @Override
            public <R> R accept(Visitor<T, R> visitor) {
                return visitor.firstNAfter(key, limit);
            }
        };
    }

    public static <T> QuerySlicing<T> lastNBefore(T key, int limit) {
        return new QuerySlicing<T>() {
            @Override
            public <R> R accept(Visitor<T, R> visitor) {
                return visitor.lastNBefore(key, limit);
            }
        };
    }

    private QuerySlicing() {
    }

    public abstract <R> R accept(Visitor<T, R> visitor);

    public <U> QuerySlicing<U> map(Function<T, U> transformation) {
        return this.accept(new Visitor<T, QuerySlicing<U>>() {
            @Override
            public QuerySlicing<U> all() {
                return QuerySlicing.all();
            }

            @Override
            public QuerySlicing<U> firstN(int limit) {
                return QuerySlicing.firstN(limit);
            }

            @Override
            public QuerySlicing<U> lastN(int limit) {
                return QuerySlicing.lastN(limit);
            }

            @Override
            public QuerySlicing<U> firstNAfter(T key, int limit) {
                return QuerySlicing.firstNAfter(transformation.apply(key), limit);
            }

            @Override
            public QuerySlicing<U> lastNBefore(T key, int limit) {
                return QuerySlicing.lastNBefore(transformation.apply(key), limit);
            }
        });
    }

    public Kind kind() {
        return this.accept(new Visitor<T, Kind>() {
            @Override
            public Kind all() {
                return Kind.ALL;
            }

            @Override
            public Kind firstN(int limit) {
                return Kind.FIRST_N;
            }

            @Override
            public Kind lastN(int limit) {
                return Kind.LAST_N;
            }

            @Override
            public Kind firstNAfter(T key, int limit) {
                return Kind.FIRST_N_AFTER;
            }

            @Override
            public Kind lastNBefore(T key, int limit) {
                return Kind.LAST_N_BEFORE;
            }
        });
    }

    public T getComparisonValue() {
        return this.accept(new Visitor<T, T>() {
            @Override
            public T all() {
                throw new UnsupportedOperationException("Check kind before calling this method");
            }

            @Override
            public T firstN(int limit) {
                throw new UnsupportedOperationException("Check kind before calling this method");
            }

            @Override
            public T lastN(int limit) {
                throw new UnsupportedOperationException("Check kind before calling this method");
            }

            @Override
            public T firstNAfter(T key, int limit) {
                return key;
            }

            @Override
            public T lastNBefore(T key, int limit) {
                return key;
            }
        });
    }

    public int getLimit() {
        return this.accept(new Visitor<T, Integer>() {
            @Override
            public Integer all() {
                throw new UnsupportedOperationException("Check kind before calling this method");
            }

            @Override
            public Integer firstN(int limit) {
                return limit;
            }

            @Override
            public Integer lastN(int limit) {
                return limit;
            }

            @Override
            public Integer firstNAfter(T key, int limit) {
                return limit;
            }

            @Override
            public Integer lastNBefore(T key, int limit) {
                return limit;
            }
        });
    }

    public interface Visitor<T, R> {
        R all();
        R firstN(int limit);
        R lastN(int limit);
        R firstNAfter(T key, int limit);
        R lastNBefore(T key, int limit);
    }

    public enum Kind {
        ALL, FIRST_N, LAST_N, FIRST_N_AFTER, LAST_N_BEFORE;

        private static final Set<Kind> HAS_LIMIT =
                Collections.unmodifiableSet(EnumSet.of(FIRST_N, FIRST_N_AFTER, LAST_N, LAST_N_BEFORE));

        private static final Set<Kind> HAS_CONDITION =
                Collections.unmodifiableSet(EnumSet.of(FIRST_N_AFTER, LAST_N_BEFORE));

        private static final Set<QuerySlicing.Kind> REVERSED_ORDER =
                Collections.unmodifiableSet(EnumSet.of(QuerySlicing.Kind.LAST_N, QuerySlicing.Kind.LAST_N_BEFORE));


        public boolean shouldBeLimited() {
            return HAS_LIMIT.contains(this);
        }

        public boolean shouldBeOrdered() {
            return shouldBeLimited();
        }

        public boolean orderIsReversed() {
            if (!shouldBeOrdered())
                throw new UnsupportedOperationException("orderIsStraight is undefined for unordered slicing");
            return !REVERSED_ORDER.contains(this);
        }

        public boolean orderIsStraight() {
            return !orderIsReversed();
        }

        public boolean shouldHaveCondition() {
            return HAS_CONDITION.contains(this);
        }

        public boolean conditionIsGreater() {
            if (!shouldHaveCondition())
                throw new UnsupportedOperationException("conditionIsGreater is undefined for slicing without condition");
            return this == FIRST_N_AFTER;
        }
        public boolean conditionIsLess() {
            return !conditionIsGreater();
        }
    }

    private static class NoSlicing<T> extends QuerySlicing<T> {
        @Override
        public <R> R accept(Visitor<T, R> visitor) {
            return visitor.all();
        }
    }
}
