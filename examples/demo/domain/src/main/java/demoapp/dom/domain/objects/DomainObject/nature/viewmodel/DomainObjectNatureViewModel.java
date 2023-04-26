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
package demoapp.dom.domain.objects.DomainObject.nature.viewmodel;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.applib.services.title.TitleService;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.domain.objects.DomainObject.nature.entity.DomainObjectNatureEntity;

//tag::class[]
@XmlRootElement(name = "root")                              // <.>
@XmlType(propOrder = {"message", "underlying"})             // <1>
@XmlAccessorType(XmlAccessType.FIELD)                       // <1>
@Named("demo.StatefulViewModelJaxbRefsEntity")
@DomainObject(nature=Nature.VIEW_MODEL)                     // <.>
@NoArgsConstructor
public class DomainObjectNatureViewModel
        implements HasAsciiDocDescription {

    public DomainObjectNatureViewModel(DomainObjectNatureEntity underlying) {
        this.message = underlying.getName();
        this.underlying = underlying;
    }

    @ObjectSupport public String title() {                  // <.>
        return message != null ? message : titleService.titleOf(underlying);
    }

    @Property
    @Getter @Setter
    @XmlElement(required = false)
    private String message;

    @Property
    @Getter @Setter
    @XmlElement
    @XmlJavaTypeAdapter(PersistentEntityAdapter.class)      // <.>
    private DomainObjectNatureEntity underlying;

    @Inject @XmlTransient TitleService titleService;
}
//end::class[]
