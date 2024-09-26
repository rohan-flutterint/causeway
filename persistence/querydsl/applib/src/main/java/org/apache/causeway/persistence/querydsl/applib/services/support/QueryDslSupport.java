/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.causeway.persistence.querydsl.applib.services.support;

import org.apache.causeway.persistence.querydsl.applib.DslQuery;

import com.querydsl.core.QueryFactory;
import com.querydsl.core.Tuple;
import com.querydsl.core.dml.DeleteClause;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;

public interface QueryDslSupport extends QueryFactory<DslQuery<?>> {

    DeleteClause<?> delete(EntityPath<?> path);

    /**
     * Create a new {@link DslQuery} instance with the given projection
     *
     * @param expr projection
     * @param <T>
     * @return select(expr)
     */
    <T> DslQuery<T> select(Expression<T> expr);

    /**
     * Create a new {@link DslQuery} instance with the given projection
     *
     * @param exprs projection
     * @return select(exprs)
     */
    DslQuery<Tuple> select(Expression<?>... exprs);

    /**
     * Create a new {@link DslQuery} instance with the given projection
     *
     * @param expr projection
     * @param <T>
     * @return select(distinct expr)
     */
    <T> DslQuery<T> selectDistinct(Expression<T> expr);

    /**
     * Create a new {@link DslQuery} instance with the given projection
     *
     * @param exprs projection
     * @return select(distinct exprs)
     */
    DslQuery<Tuple> selectDistinct(Expression<?>... exprs);

    /**
     * Create a new {@link DslQuery} instance with the projection 0
     *
     * @return select(0)
     */
    DslQuery<Integer> selectZero();

    /**
     * Create a new {@link DslQuery} instance with the projection 1
     *
     * @return select(1)
     */
    DslQuery<Integer> selectOne();

    /**
     * Create a new {@link DslQuery} instance with the given projection
     *
     * @param expr projection and source
     * @param <T>
     * @return select(expr).from(expr)
     */
    <T> DslQuery<T> selectFrom(EntityPath<T> expr);

    <T> DslQuery<T> from(EntityPath<T> from);
    <T> DslQuery<T> from(EntityPath<T>... from);
}
