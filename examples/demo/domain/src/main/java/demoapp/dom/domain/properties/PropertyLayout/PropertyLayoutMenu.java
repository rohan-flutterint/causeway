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
package demoapp.dom.domain.properties.PropertyLayout;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;

import lombok.RequiredArgsConstructor;
import lombok.val;

import demoapp.dom.domain.properties.PropertyLayout.cssClass.PropertyLayoutCssClassPage;
import demoapp.dom.domain.properties.PropertyLayout.describedAs.PropertyLayoutDescribedAsPage;
import demoapp.dom.domain.properties.PropertyLayout.hidden.PropertyLayoutHiddenPage;
import demoapp.dom.domain.properties.PropertyLayout.labelPosition.PropertyLayoutLabelPositionPage;
import demoapp.dom.domain.properties.PropertyLayout.multiLine.PropertyLayoutMultiLinePage;
import demoapp.dom.domain.properties.PropertyLayout.named.PropertyLayoutNamedPage;
import demoapp.dom.domain.properties.PropertyLayout.navigable.FileNodeVm;
import demoapp.dom.domain.properties.PropertyLayout.repainting.PropertyLayoutRepaintingPage;
import demoapp.dom.domain.properties.PropertyLayout.typicalLength.PropertyLayoutTypicalLengthPage;
import demoapp.dom.types.Samples;

@Named("demo.PropertyLayoutMenu")
@DomainService(
        nature=NatureOfService.VIEW
)
@jakarta.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
//@Log4j2
public class PropertyLayoutMenu {

    final Samples<Blob> samples;


    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-pen-nib", describedAs = "CSS class to wrap the UI component representing this property")
    public PropertyLayoutCssClassPage cssClass(){
        return new PropertyLayoutCssClassPage();
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-comment", describedAs = "Description of the property, shown as a tooltip")
    public PropertyLayoutDescribedAsPage describedAs(){
        return new PropertyLayoutDescribedAsPage();
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-glasses", describedAs = "Visibility of the property in different contexts")
    public PropertyLayoutHiddenPage hidden() {
        return new PropertyLayoutHiddenPage();
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-tag", describedAs = "Label positions")
    public PropertyLayoutLabelPositionPage labelPosition(){
        return new PropertyLayoutLabelPositionPage();
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-align-justify", describedAs = "Textboxes")
    public PropertyLayoutMultiLinePage multiLine(){
        val vm = new PropertyLayoutMultiLinePage();
        vm.setPropertyUsingAnnotation(
                "A multiline string\n" +
                "spanning\n" +
                "5 lines\n" +
                "\n" +
                "(click me to edit)\n"
                );
        vm.setPropertyUsingAnnotationReadOnly(
                "A readonly string\n" +
                "spanning\n" +
                "5 lines\n" +
                "\n" +
                "(Allows text select)");
        return vm;
    }


    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-signature", describedAs = "Custom text for the property's label")
    public PropertyLayoutNamedPage named(){
        return new PropertyLayoutNamedPage();
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-sitemap", describedAs = "Breadcrumbs (and trees)")
    public FileNodeVm navigable(){
        return new FileNodeVm();
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-paint-brush", describedAs = "Performance hint for properties holding unchanging large objects")
    public PropertyLayoutRepaintingPage repainting(){
        val vm = new PropertyLayoutRepaintingPage();
        vm.setEditMe("Modify this field to see if repainting occurs...");
        samples.stream()
                .filter(x -> CommonMimeType.PDF.matches(x.getMimeType()))
                .findFirst()
                .ifPresent(pdfBlob -> {
                    vm.setBlob(pdfBlob);
                });
        return vm;
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-ruler-horizontal", describedAs = "Length of text fields")
    public PropertyLayoutTypicalLengthPage typicalLength(){
        val page = new PropertyLayoutTypicalLengthPage();
        page.setName("abcdefghij");
        page.setNotes("abcdefghij");
        return page;
    }


}
