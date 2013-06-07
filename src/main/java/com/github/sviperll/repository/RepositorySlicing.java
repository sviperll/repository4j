package com.github.sviperll.repository;

public interface RepositorySlicing<T> {
    public boolean isOrdered();

    public boolean isDescending();

    public boolean hasLimit();

    public int limit();

    public boolean hasConditions();

    public RepositorySlicingCondition<T> condition();

    public RepositorySlicingResult result();

    public static interface RepositorySlicingCondition<T> {
        public boolean isLess();

        public boolean isGreater();

        public T value();
    }

    public interface RepositorySlicingResult {
        public boolean needToBeReveresed();
    }
}
