/*
 * Copyright (c) 2014, Victor Nazarov <asviraspossible@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation and/or
 *     other materials provided with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.sviperll.repository;

import com.github.sviperll.OptionalVisitor;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public interface SQLSupport {
    SQLConnection connection();

    <K, V, O> List<V> entryList(ReadableRepositoryDirectoryConfiguration<K, V, O> configuration,
                                K key,
                                SlicingQuery<O> slicing) throws SQLException;

    <K, V, R, E extends Exception> R get(IndexedRepositoryConfiguration<K, V> configuration,
                                         K key,
                                         OptionalVisitor<V, R, E> optionalVisitor) throws E, SQLException;

    <K, V> boolean putIfExists(IndexedRepositoryConfiguration<K, V> configuration,
                               K key,
                               Changes<V> attributes) throws SQLException;

    <K, V> K putNewEntry(AutogeneratedKeyIndexedRepositoryConfiguration<K, V> configuration, V attributes)
            throws SQLException;

    <K, V> void putNewEntry(IndexedRepositoryConfiguration<K, V> configuration, K key, V attributes)
            throws SQLException;

    <K> boolean remove(RepositoryIndex<K> configuration, K key) throws SQLException;
}
