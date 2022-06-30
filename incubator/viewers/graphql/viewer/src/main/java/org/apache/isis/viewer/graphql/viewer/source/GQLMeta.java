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
package org.apache.isis.viewer.graphql.viewer.source;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;

import lombok.Data;
import org.apache.isis.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

@Data
public class GQLMeta {

    private final Bookmark bookmark;
    private final BookmarkService bookmarkService;
    private final ObjectTypeDataCollector dataCollector;

    public String logicalTypeName(){
        return bookmark.getLogicalTypeName();
    }

    public String id(){
        return bookmark.getIdentifier();
    }

    public String version(){
        Object domainObject = bookmarkService.lookup(bookmark).orElse(null);
        if (domainObject == null) return null;

        // TODO: implement; we would like to be this independent of the persistence mechanism
        return "not yet implemented";
    }

    public String iconName(){
        //Todo : implement
        return "not yet implemented";
    }

    public GQLMetaStructure structure(){
        return new GQLMetaStructure(dataCollector);
    };

    public String title(){
        Object domainObject = bookmarkService.lookup(bookmark).orElse(null);
        if (domainObject == null) return null;
        return dataCollector.getObjectSpecification().getTitleService().titleOf(domainObject);
    }

    public GQLMetaFields fields(){
        //TODO: this is dynamic Maybe pass in ObjectTypeDataCollector (also in GQLMutations)
        return new GQLMetaFields();
    }

}
