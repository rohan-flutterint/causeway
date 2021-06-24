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
package org.apache.isis.viewer.common.model.decorator.icon;

import java.net.URL;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.resources._Resources;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.val;

@Service
@Named("isis.viewer.common.ObjectIconServiceDefault")
@Priority(PriorityPrecedence.LATE)
@Qualifier("Default")
//@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ObjectIconServiceDefault
implements ObjectIconService {

    private static final String DEFAULT_IMAGE_RESOURCE_PATH = "/images";
    private static final Can<CommonMimeType> IMAGE_TYPES =
            Can.of(
                CommonMimeType.PNG,
                CommonMimeType.GIF,
                CommonMimeType.JPEG,
                CommonMimeType.SVG);

    private final Map<String, ObjectIcon> iconByKey = _Maps.newConcurrentHashMap();

    private final ObjectIcon fallbackIcon =
            new ObjectIcon(
                    "ObjectIconFallback",
                    _Resources.getResourceUrl(
                            ObjectIconServiceDefault.class,
                            "ObjectIconFallback.png"),
                    CommonMimeType.PNG);


    @Override
    public ObjectIcon getObjectIcon(
            final @NonNull ObjectSpecification spec,
            final @Nullable String iconNameModifier) {

        val domainClass = spec.getCorrespondingClass();
        val iconResourceKey = _Strings.isNotEmpty(iconNameModifier)
                ? domainClass.getName() + "-" + iconNameModifier
                : domainClass.getName();

        // also memoize unsuccessful icon lookups, so we don't search repeatedly
        return iconByKey.computeIfAbsent(iconResourceKey, key->
            findIcon(spec, iconNameModifier));
    }

    @Override
    public ObjectIcon getObjectFallbackIcon() {
        return fallbackIcon;
    }

    // -- HELPER

    private ObjectIcon findIcon(
            final @NonNull ObjectSpecification spec,
            final @Nullable String iconNameModifier) {

        val domainClass = spec.getCorrespondingClass();
        val iconResourceNameNoExt = _Strings.isNotEmpty(iconNameModifier)
                ? domainClass.getSimpleName() + "-" + iconNameModifier
                : domainClass.getSimpleName();

        for(val imageType : IMAGE_TYPES) {

            val objectIcon1 = imageType
                .getProposedFileExtensions()
                .stream()
                .map(suffix->iconResourceNameNoExt + "." + suffix)
                .map(iconResourceName->
                        classPathResource(domainClass, iconResourceName)
                        .map(url->new ObjectIcon(
                                iconResourceNameNoExt,
                                url,
                                imageType)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

            if(objectIcon1.isPresent()) {
                return objectIcon1.get(); // short-circuit if found
            }

            // also search the default image resource path

            val objectIcon2 = imageType
                    .getProposedFileExtensions()
                    .stream()
                    .map(suffix->DEFAULT_IMAGE_RESOURCE_PATH + "/" + iconResourceNameNoExt + "." + suffix)
                    .map(iconResourcePath->
                            classPathResource(iconResourcePath)
                            .map(url->new ObjectIcon(
                                    iconResourceNameNoExt,
                                    url,
                                    imageType)))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();

            if(objectIcon2.isPresent()) {
                return objectIcon2.get(); // short-circuit if found
            }

        }

        return spec.superclass()!=null
                // continue search in super spec
                ? getObjectIcon(spec.superclass(), iconNameModifier) // memoizes as a side-effect
                : _Strings.isNotEmpty(iconNameModifier)
                        // also do a more generic search, skipping the modifier
                        ? getObjectIcon(spec, null) // memoizes as a side-effect
                        : getObjectFallbackIcon();
    }

    // -- HELPER

    private static Optional<URL> classPathResource(
            final @NonNull String absoluteResourceName) {
        if(!absoluteResourceName.startsWith("/")) {
            throw _Exceptions
            .illegalArgument("invalid absolute resourceName %s", absoluteResourceName);
        }
        val resourceUrl = _Context.getDefaultClassLoader().getResource(absoluteResourceName);
        return Optional.ofNullable(resourceUrl);
    }

    private static Optional<URL> classPathResource(
            final @NonNull Class<?> contextClass,
            final @NonNull String relativeResourceName) {
        if(relativeResourceName.startsWith("/")) {
            throw _Exceptions
            .illegalArgument("invalid relative resourceName %s", relativeResourceName);
        }
        val resourceUrl = _Resources.getResourceUrl(contextClass, relativeResourceName);
        return Optional.ofNullable(resourceUrl);
    }

}
