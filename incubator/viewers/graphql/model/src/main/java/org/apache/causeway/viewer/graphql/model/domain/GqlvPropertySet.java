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

import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.val;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;

import java.util.Map;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

public class GqlvPropertySet {

    final Holder holder;
    final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    final SpecificationLoader specificationLoader;
    final GraphQLFieldDefinition field;

    public GqlvPropertySet(
            final Holder holder,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final SpecificationLoader specificationLoader) {
        this.holder = holder;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.field = fieldDefinition(holder);
        this.specificationLoader = specificationLoader;
    }

    GraphQLFieldDefinition fieldDefinition(final Holder holder) {

        GraphQLFieldDefinition fieldDefinition = null;
        GraphQLOutputType type = outputTypeFor(holder);
        if (type != null) {
            val fieldBuilder = newFieldDefinition()
                    .name("set")
                    .type(type);
            GqlvProperty.addGqlArgument(holder.getOneToOneAssociation(), fieldBuilder, TypeMapper.InputContext.INVOKE);
            fieldDefinition = fieldBuilder.build();

            holder.addField(fieldDefinition);
        }
        return fieldDefinition;
    }

    GraphQLOutputType outputTypeFor(Holder holder) {
        return TypeMapper.outputTypeFor(holder.getObjectSpecification());   // setters return void, so we return the domain object instead
    }


    void addDataFetcher() {

        val association = holder.getOneToOneAssociation();
        val fieldObjectSpecification = association.getElementType();
        val beanSort = fieldObjectSpecification.getBeanSort();

        switch (beanSort) {
            case VALUE:
            case VIEW_MODEL:
            case ENTITY:
                codeRegistryBuilder.dataFetcher(
                        holder.coordinatesFor(field),
                        this::set);

                break;
        }
    }

    Object set(final DataFetchingEnvironment dataFetchingEnvironment) {

        val association = holder.getOneToOneAssociation();

        val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        val sourcePojoClass = sourcePojo.getClass();
        val objectSpecification = specificationLoader.loadSpecification(sourcePojoClass);
        if (objectSpecification == null) {
            // not expected
            return null;
        }

        val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);

        Map<String, Object> arguments = dataFetchingEnvironment.getArguments();
        Object argumentValue = arguments.get(association.getId());
        ManagedObject argumentManagedObject = ManagedObject.adaptProperty(association, argumentValue);

        Consent consent = association.isAssociationValid(managedObject, argumentManagedObject, InteractionInitiatedBy.USER);
        if (consent.isVetoed()) {
            throw new IllegalArgumentException(consent.getReasonAsString().orElse("Invalid"));
        }

        association.set(managedObject, argumentManagedObject, InteractionInitiatedBy.USER);

        return managedObject; // return the original object because setters return void
    }

    public interface Holder extends GqlvHolder {

        GqlvProperty.Holder getHolder();

        ObjectSpecification getObjectSpecification();

        OneToOneAssociation getOneToOneAssociation();

    }
}
