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
package demoapp.dom.domain.objects.DomainObject.xxxLifecycleEvent;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

//tag::class[]
@XmlRootElement(name = "demo.DomainObjectLifecycleEventVm")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.DomainObjectLifecycleEventVm")
@DomainObject(nature = Nature.VIEW_MODEL)
@NoArgsConstructor
public class DomainObjectLifecycleEventVm implements HasAsciiDocDescription {

    public DomainObjectLifecycleEventVm(String eventType, String bookmark) {
        this.eventType = eventType;
        this.bookmark = bookmark;
    }

    @ObjectSupport public String title() {
        return eventType + ": " + bookmark;
    }

    @Property
    @Getter @Setter
    private String eventType;

    @Property
    @Getter @Setter
    private String bookmark;

}
//end::class[]
