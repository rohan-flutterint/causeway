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
package org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns;

import java.util.Optional;

import org.apache.wicket.Component;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.models.interaction.coll.DataRowWkt;
import org.apache.causeway.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.causeway.viewer.wicket.ui.components.actionmenu.entityactions.LinkAndLabelFactory;

import lombok.NonNull;

public final class ActionColumn
extends GenericColumnAbstract {

    private static final long serialVersionUID = 1L;

    public static Optional<ActionColumn> create(
            @NonNull final Identifier featureId,
            @NonNull final ObjectSpecification elementType) {
        var actions = elementType.streamActionsForColumnRendering(featureId)
                .collect(Can.toCan());
        if(actions.isEmpty()) return Optional.empty();
        return Optional.of(new ActionColumn(elementType, actions));
    }

    private final Can<String> actionIds;
    private transient Can<ObjectAction> actions;

    private ActionColumn(
            final ObjectSpecification elementType,
            final Can<ObjectAction> actionsForColumnRendering) {
        super(elementType, "Actions");
        this.actions = actionsForColumnRendering;
        this.actionIds = actions.map(ObjectAction::getId);
    }

    @Override
    protected Component createCellComponent(
            final String componentId, final DataRowWkt dataRowWkt) {
        var dataRow = dataRowWkt.getObject();
        var rowElement = dataRow.getRowElement();

        var entityModel = UiObjectWkt.ofAdapter(rowElement);
        var linksAndLables = actions().stream()
            .map(LinkAndLabelFactory.forEntity(entityModel))
            .collect(Can.toCan());
        
        return AdditionalLinksPanel.Style.DROPDOWN.newPanel(componentId, linksAndLables);
    }

    // -- HELPER

    private Can<ObjectAction> actions() {
        synchronized(this) {
            if(actions==null) {
                var elementTypeSpec = elementType();
                this.actions = actionIds.map(elementTypeSpec::getActionElseFail);
            }
        }
        return actions;
    }

}
