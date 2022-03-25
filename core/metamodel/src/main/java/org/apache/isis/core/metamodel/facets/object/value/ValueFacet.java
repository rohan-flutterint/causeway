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
package org.apache.isis.core.metamodel.facets.object.value;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.value.semantics.DefaultsProvider;
import org.apache.isis.applib.value.semantics.OrderRelation;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider.Context;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.delegate._Delegate;
import org.apache.isis.core.metamodel.commons.CanonicalInvoker;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.UnwrapUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember.AuthorizationException;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionMixedIn;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.Delegate;

/**
 * Indicates that this class has value semantics.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to the
 * <tt>@Value</tt> annotation. However, note that value semantics is just a
 * convenient term for a number of mostly optional semantics all of which are
 * defined elsewhere.
 */
public interface ValueFacet<T>
extends
    ValueSerializer<T>,
    Facet {

    LogicalType getLogicalType();

    Can<ValueSemanticsProvider<T>> getAllValueSemantics();

    Context createValueSemanticsContext(@Nullable ObjectFeature feature);
    <X> Stream<X> streamValueSemantics(Class<X> requiredType);

    /** no qualifiers allowed on the default semantics provider*/
    Optional<ValueSemanticsProvider<T>> selectDefaultSemantics();

    // -- ORDER RELATION

    /** no qualifiers allowed on the default semantics provider*/
    Optional<OrderRelation<T, ?>> selectDefaultOrderRelation();

    // -- DEFAULTS PROVIDER

    /** no qualifiers allowed on the default semantics provider*/
    Optional<DefaultsProvider<T>> selectDefaultDefaultsProvider();

    // -- PARSER

    /** no qualifiers allowed on the default semantics provider*/
    Optional<Parser<T>> selectDefaultParser();
    Optional<Parser<T>> selectParserForParameter(final ObjectActionParameter param);
    Optional<Parser<T>> selectParserForProperty(final OneToOneAssociation prop);

    default Optional<Parser<T>> selectParserForFeature(final @Nullable ObjectFeature feature) {
        if(feature==null) {
            return selectDefaultParser();
        }
        switch(feature.getFeatureType()) {
        case ACTION_PARAMETER_SCALAR:
            return selectParserForParameter((ObjectActionParameter)feature);
        case PROPERTY:
            return selectParserForProperty((OneToOneAssociation)feature);
        default:
            return selectDefaultParser();
        }
    }

    Parser<T> fallbackParser(Identifier featureIdentifier);

    default Parser<T> selectParserForParameterElseFallback(final ObjectActionParameter param) {
        return selectParserForParameter(param)
                .orElseGet(()->fallbackParser(param.getFeatureIdentifier()));
    }

    default Parser<T> selectParserForPropertyElseFallback(final OneToOneAssociation prop) {
        return selectParserForProperty(prop)
                .orElseGet(()->fallbackParser(prop.getFeatureIdentifier()));
    }

    default Parser<T> selectParserForFeatureElseFallback(final ObjectFeature feature) {
        return selectParserForFeature(feature)
                .orElseGet(()->fallbackParser(feature.getFeatureIdentifier()));
    }

    // -- RENDERER

    /** no qualifiers allowed on the default semantics provider*/
    Optional<Renderer<T>> selectDefaultRenderer();
    Optional<Renderer<T>> selectRendererForParameter(final ObjectActionParameter param);
    Optional<Renderer<T>> selectRendererForProperty(final OneToOneAssociation prop);

    Renderer<T> fallbackRenderer(Identifier featureIdentifier);

    default Optional<Renderer<T>> selectRendererForFeature(final @Nullable ObjectFeature feature) {
        if(feature==null) {
            return selectDefaultRenderer();
        }
        switch(feature.getFeatureType()) {
        case ACTION_PARAMETER_SCALAR:
            return selectRendererForParameter((ObjectActionParameter)feature);
        case PROPERTY:
            return selectRendererForProperty((OneToOneAssociation)feature);
        default:
            return selectDefaultRenderer();
        }
    }

    default Renderer<T> selectRendererForParameterElseFallback(final ObjectActionParameter param) {
        return selectRendererForParameter(param)
                .orElseGet(()->fallbackRenderer(param.getFeatureIdentifier()));
    }

    default Renderer<T> selectRendererForPropertyElseFallback(final OneToOneAssociation prop) {
        return selectRendererForProperty(prop)
                .orElseGet(()->fallbackRenderer(prop.getFeatureIdentifier()));
    }

    // -- COMPOSITE VALUE SUPPORT

    default boolean isCompositeValueType() {
        return selectDefaultSemantics()
        .map(valueSemantics->valueSemantics.getSchemaValueType()==ValueType.COMPOSITE)
        .orElse(false);
    }

    default Optional<ObjectAction> selectCompositeValueMixinForFeature(final ManagedProperty managedProperty) {
        if(!isCompositeValueType()) {
            return Optional.empty();
        }
        //feed the action's invocation result back into the scalarModel's proposed value, then submit

        //XXX the mixin is found on the value-type's (eg on CalendarEvent) ObjectSpecifcation
        //no customization support yet
        return managedProperty.getElementType().getAction("updatec")
        .map(m->wrap(managedProperty, (ObjectActionMixedIn) m));
    }

    static interface X {
      Identifier getFeatureIdentifier();
      ObjectSpecification getReturnType();
      ManagedObject executeWithRuleChecking(final InteractionHead head, final Can<ManagedObject> parameters,
              final InteractionInitiatedBy interactionInitiatedBy, final Where where) throws AuthorizationException;
      ManagedObject execute(final InteractionHead head, final Can<ManagedObject> parameters,
              final InteractionInitiatedBy interactionInitiatedBy);
    }

    @SuppressWarnings("unused")
    @RequiredArgsConstructor
    public static class CompositeValueUpdateAction {

        private final ManagedProperty managedProperty;

        @Delegate(excludes=X.class)
        private final ObjectActionMixedIn delegate;

        public ObjectSpecification getReturnType() {
            return managedProperty.getElementType();
        }

        public Identifier getFeatureIdentifier() {
            val id = delegate.getFeatureIdentifier();
            return Identifier
                    .actionIdentifier(
                            id.getLogicalType(),
                            id.getMemberLogicalName(),
                            id.getMemberParameterClassNames());
        }

        public ManagedObject executeWithRuleChecking(
                final InteractionHead head, final Can<ManagedObject> parameters,
                final InteractionInitiatedBy interactionInitiatedBy, final Where where)
                        throws AuthorizationException {
//            val valueType = delegate
//                    .execute(head, parameters, interactionInitiatedBy);
            return map(simpleExecute(head, parameters));
        }

        public ManagedObject execute(
                final InteractionHead head, final Can<ManagedObject> parameters,
                final InteractionInitiatedBy interactionInitiatedBy) {
//            val valueType = delegate
//                    .execute(head, parameters, interactionInitiatedBy);
            return map(simpleExecute(head, parameters));
        }

        private ManagedObject simpleExecute(
                final InteractionHead head, final Can<ManagedObject> parameters) {
            val method = delegate.getFacetedMethod().getMethod();

            final Object[] executionParameters = UnwrapUtil.multipleAsArray(parameters);
            final Object targetPojo = UnwrapUtil.single(head.getTarget());

            val resultPojo = CanonicalInvoker
                    .invoke(method, targetPojo, executionParameters);

            return ManagedObject.of(delegate.getReturnType(), resultPojo);
        }

        private ManagedObject map(final ManagedObject valueType) {
            val propNeg = managedProperty.startNegotiation();
            propNeg.getValue().setValue(valueType);
            propNeg.submit();
            val propertyOwnerSpec = managedProperty.getOwner().getSpecification();
            return managedProperty.getOwner();
        }

    }

    static ObjectAction wrap(
            final ManagedProperty managedProperty,
            final ObjectActionMixedIn delegate) {
        return _Delegate.createProxy(ObjectAction.class,
                new CompositeValueUpdateAction(managedProperty, delegate));
    }

}
