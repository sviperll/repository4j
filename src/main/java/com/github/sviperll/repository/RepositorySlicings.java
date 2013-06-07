/*
 * Copyright (C) 2013 Victor Nazarov <asviraspossible@gmail.com>
 */

package com.github.sviperll.repository;

import com.github.sviperll.repository.RepositorySlicing.RepositorySlicingResult;

public class RepositorySlicings {
    private static final NotNeededToBeReversedRepositorySlicingResult NOT_NEEDED_TO_BE_REVERESED_REPOSITORY_SLICING_RESULT = new NotNeededToBeReversedRepositorySlicingResult();

    @SuppressWarnings("rawtypes")
    private static final UnlimitedRepositorySlicing UNLIMITED_REPOSITORY_SLICING = new UnlimitedRepositorySlicing();

    public static <T> RepositorySlicing<T> firstN(final int limit) {
        return new FirstNRepositorySlicing<T>(limit);
    }

    @SuppressWarnings("unchecked")
    public static <T> RepositorySlicing<T> unlimited() {
        return UNLIMITED_REPOSITORY_SLICING;
    }

    private static RepositorySlicingResult notNeededToBeReversedResult() {
        return NOT_NEEDED_TO_BE_REVERESED_REPOSITORY_SLICING_RESULT;
    }

    public static <T> RepositorySlicing<T> firstNAfter(final int limit, final T value) {
        return new FirstNAfterRepositorySlicing<T>(limit, value);
    }

    private RepositorySlicings() {
    }

    private static class FirstNRepositorySlicing<T> implements RepositorySlicing<T> {
        private final int limit;

        public FirstNRepositorySlicing(int limit) {
            this.limit = limit;
        }

        @Override
        public boolean isOrdered() {
            return true;
        }

        @Override
        public boolean isDescending() {
            return false;
        }

        @Override
        public boolean hasLimit() {
            return true;
        }

        @Override
        public int limit() {
            return limit;
        }

        @Override
        public boolean hasConditions() {
            return false;
        }

        @Override
        public RepositorySlicingCondition<T> condition() {
            throw new UnsupportedOperationException("Has no conditions.");
        }

        @Override
        public RepositorySlicingResult result() {
            return RepositorySlicings.notNeededToBeReversedResult();
        }
    }

    private static class UnlimitedRepositorySlicing<T> implements RepositorySlicing<T> {
        public UnlimitedRepositorySlicing() {
        }

        @Override
        public boolean isOrdered() {
            return false;
        }

        @Override
        public boolean isDescending() {
            throw new UnsupportedOperationException("Is not ordered.");
        }

        @Override
        public boolean hasLimit() {
            return false;
        }

        @Override
        public int limit() {
            throw new UnsupportedOperationException("Has no limit.");
        }

        @Override
        public boolean hasConditions() {
            return false;
        }

        @Override
        public RepositorySlicingCondition<T> condition() {
            throw new UnsupportedOperationException("Has no conditions.");
        }

        @Override
        public RepositorySlicingResult result() {
            return RepositorySlicings.notNeededToBeReversedResult();
        }
    }

    private static class NotNeededToBeReversedRepositorySlicingResult implements RepositorySlicingResult {
        @Override
        public boolean needToBeReveresed() {
            return false;
        }
    }

    private static class FirstNAfterRepositorySlicing<T> implements RepositorySlicing<T> {
        private final int limit;
        private final T value;

        public FirstNAfterRepositorySlicing(int limit, T value) {
            this.limit = limit;
            this.value = value;
        }

        @Override
        public boolean isOrdered() {
            return true;
        }

        @Override
        public boolean isDescending() {
            return false;
        }

        @Override
        public boolean hasLimit() {
            return true;
        }

        @Override
        public int limit() {
            return limit;
        }

        @Override
        public boolean hasConditions() {
            return true;
        }

        @Override
        public RepositorySlicingCondition<T> condition() {
            return new RepositorySlicingCondition<T>() {
                @Override
                public boolean isLess() {
                    return false;
                }

                @Override
                public boolean isGreater() {
                    return true;
                }

                @Override
                public T value() {
                    return value;
                }
            };
        }

        @Override
        public RepositorySlicingResult result() {
            return RepositorySlicings.notNeededToBeReversedResult();
        }
    }
}
