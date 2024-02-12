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
 */
package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojoFetcher;

public class GqlvCollection
        extends GqlvAssociation<OneToManyAssociation, GqlvCollection.Holder>
        implements GqlvCollectionGet.Holder,
                   GqlvMemberHidden.Holder<OneToManyAssociation>,
                   GqlvMemberDisabled.Holder<OneToManyAssociation> {

    private final GqlvMemberHidden<OneToManyAssociation> hidden;
    private final GqlvMemberDisabled<OneToManyAssociation> disabled;
    private final GqlvCollectionGet get;

    public GqlvCollection(
            final Holder holder,
            final OneToManyAssociation oneToManyAssociation,
            final Context context) {
        super(holder, oneToManyAssociation, newObject().name(TypeNames.collectionTypeNameFor(holder.getObjectSpecification(), oneToManyAssociation)), context);

        this.hidden = new GqlvMemberHidden<>(this, context);
        addField(hidden.getField());
        this.disabled = new GqlvMemberDisabled<>(this, context);
        addField(disabled.getField());
        this.get = new GqlvCollectionGet(this, context);
        addField(get.getField());

        buildObjectTypeAndSetFieldName(oneToManyAssociation.getId());
    }

    @Override
    public ObjectSpecification getObjectSpecification() {
        return holder.getObjectSpecification();
    }

    public OneToManyAssociation getOneToManyAssociation() {
        return getObjectAssociation();
    }

    public void addDataFetcher() {
        context.codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(getField()),
                new BookmarkedPojoFetcher(context.bookmarkService));

        hidden.addDataFetcher();
        disabled.addDataFetcher();
        get.addDataFetcher();
    }

    public interface Holder extends GqlvAssociation.Holder {

    }
}
