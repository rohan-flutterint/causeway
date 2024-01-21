package org.apache.causeway.viewer.graphql.model.domain;

import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;

import graphql.schema.GraphQLObjectType;

import lombok.Getter;
import lombok.val;

import java.util.Objects;
import java.util.Optional;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvMeta {

    static GraphQLFieldDefinition id = newFieldDefinition().name("id").type(nonNull(Scalars.GraphQLString)).build();
    static GraphQLFieldDefinition logicalTypeName = newFieldDefinition().name("logicalTypeName").type(nonNull(Scalars.GraphQLString)).build();
    static GraphQLFieldDefinition version = newFieldDefinition().name("version").type(Scalars.GraphQLString).build();

    private final GqlvDomainObject domainObject;

    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private final BookmarkService bookmarkService;
    private final ObjectManager objectManager;

    @Getter private final GraphQLFieldDefinition metaField;

    public GqlvMeta(
            final GqlvDomainObject domainObject,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final BookmarkService bookmarkService,
            final ObjectManager objectManager
    ) {
        this.domainObject = domainObject;

        this.codeRegistryBuilder = codeRegistryBuilder;
        this.bookmarkService = bookmarkService;
        this.objectManager = objectManager;

        metaField = newFieldDefinition().name("_gql_meta").type(buildMetaType()).build();
    }

    GraphQLObjectType getMetaType() {
        return (GraphQLObjectType) metaField.getType();
    }

    private GraphQLObjectType buildMetaType() {
        val metaTypeBuilder = newObject().name(TypeNames.metaTypeNameFor(domainObject.getObjectSpecification()));
        metaTypeBuilder.field(id);
        metaTypeBuilder.field(logicalTypeName);
        if (domainObject.getBeanSort() == BeanSort.ENTITY) {
            metaTypeBuilder.field(version);
        }
        return metaTypeBuilder.build();
    }

    public void addDataFetchers() {

        codeRegistryBuilder.dataFetcher(
                coordinates(domainObject.getGqlObjectType(), getMetaField()),
                (DataFetcher<Object>) environment -> {
                    return bookmarkService.bookmarkFor(environment.getSource())
                            .map(bookmark -> new Fetcher(bookmark, bookmarkService, objectManager))
                            .orElse(null); //TODO: is this correct ?
                });

        codeRegistryBuilder.dataFetcher(
                coordinates(getMetaType(), id),
                (DataFetcher<Object>) environment -> environment.<Fetcher>getSource().id());

        codeRegistryBuilder.dataFetcher(
                coordinates(getMetaType(), logicalTypeName),
                (DataFetcher<Object>) environment -> environment.<Fetcher>getSource().logicalTypeName());

        if (domainObject.getBeanSort() == BeanSort.ENTITY) {
            codeRegistryBuilder.dataFetcher(
                    coordinates(getMetaType(), version),
                    (DataFetcher<Object>) environment -> environment.<Fetcher>getSource().version());
        }
    }

    /**
     * Metadata for every domain object.
     */
    static class Fetcher {

        private final Bookmark bookmark;
        private final BookmarkService bookmarkService;
        private final ObjectManager objectManager;

        Fetcher(
                final Bookmark bookmark,
                final BookmarkService bookmarkService,
                final ObjectManager objectManager) {
            this.bookmark = bookmark;
            this.bookmarkService = bookmarkService;
            this.objectManager = objectManager;
        }

        public String logicalTypeName(){
            return bookmark.getLogicalTypeName();
        }

        public String id(){
            return bookmark.getIdentifier();
        }

        public String version(){
            Object domainObject = bookmarkService.lookup(bookmark).orElse(null);
            if (domainObject == null) {
                return null;
            }
            EntityFacet entityFacet = objectManager.adapt(domainObject).getSpecification().getFacet(EntityFacet.class);
            return Optional.ofNullable(entityFacet)
                    .map(x -> x.versionOf(domainObject))
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .orElse(null);
        }

    }
}
