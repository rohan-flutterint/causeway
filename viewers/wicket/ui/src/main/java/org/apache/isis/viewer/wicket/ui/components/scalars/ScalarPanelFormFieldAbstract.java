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
package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.springframework.lang.Nullable;

import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.util.CommonContextUtils;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.CompactFragment;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.FrameFragment;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.InputFragment;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.RegularFrame;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.isis.viewer.wicket.ui.util.Wkt;
import org.apache.isis.viewer.wicket.ui.util.WktTooltips;

import lombok.val;

public abstract class ScalarPanelFormFieldAbstract<T>
extends ScalarPanelAbstract2 {

    private static final long serialVersionUID = 1L;

    protected final Class<T> type;

    protected ScalarPanelFormFieldAbstract(
            final String id,
            final ScalarModel scalarModel,
            final Class<T> type) {
        super(id, scalarModel);
        this.type = type;
    }

    @Override
    protected final Component getValidationFeedbackReceiver() {
        return getFormComponent();
    }

    // -- FORM COMPONENT

    private FormComponent<T> formComponent;
    @Nullable
    protected final FormComponent<T> getFormComponent() { return formComponent; }

    /**
     * Builds the component to render the form input field.
     */
    protected abstract FormComponent<T> createFormComponent(String id, ScalarModel scalarModel);

    // -- REGULAR

    @Override
    protected final MarkupContainer createRegularFrame() {
        val scalarModel = scalarModel();

        val friendlyNameModel = Model.of(scalarModel.getFriendlyName());

        formComponent = createFormComponent(ID_SCALAR_VALUE, scalarModel);
        formComponent.setLabel(friendlyNameModel);

        val formGroup = FrameFragment.REGULAR
                .createComponent(id->new FormGroup(id, formComponent));
        formGroup.add(formComponent);

        formComponent.setRequired(scalarModel.isRequired());
        if(scalarModel.isRequired()
                && scalarModel.isEnabled()) {
            Wkt.cssAppend(formGroup, "mandatory");
        }

        formGroup.add(createScalarNameLabel(ID_SCALAR_NAME, friendlyNameModel));

        formComponent.add(createValidator(scalarModel));

        val renderScenario = getRenderScenario();

        Wkt.labelAdd(formGroup, "debugLabel", String.format("%s", renderScenario.name()));

//        switch (renderScenario) {
//        case READONLY:
//            // setup as output-format (no links)
//            // that is: disable links - place output-format into RegularFrame.INPUT_FORMAT_CONTAINER
//            formGroup.add(RegularFrame.INPUT_FORMAT_CONTAINER
//                    .createComponent(this::createComponentForOutput));
//
//            //RegularFrame.SCALAR_VALUE_INLINE_PROMPT_LINK.permanentlyHideIn(formGroup);
//
//            break;
//        case CAN_EDIT:
//            // setup as output-format (with links to edit)
//            // that is: enable links - place output-format into RegularFrame.OUTPUT_FORMAT_CONTAINER
//            // this is done by the inline prompt setup later
//            // hide formgr comp
//            //formComponent.setVisibilityAllowed(false);
//            RegularFrame.INPUT_FORMAT_CONTAINER.permanentlyHideIn(formGroup);
//
//            break;
//        case EDITING:
//            // setup as input-format
//            // that is: disable links - place input-format into RegularFrame.INPUT_FORMAT_CONTAINER
//            getInputFragmentType()
//                .ifPresent(inputFragmentType->
//                    formGroup.add(inputFragmentType.createFragment(this, formComponent)));
//            break;
//
//        default:
//            break;
//        }

        if(scalarModel().isViewMode()
                //TODO remove this non intuitive logic
                && getFormatModifiers().contains(FormatModifier.MARKUP)) {
            //setRegularFrame(formGroup);
            formGroup.add(RegularFrame.INPUT_FORMAT_CONTAINER
                    .createComponent(this::createComponentForOutput));
        } else {
            getInputFragmentType()
            .ifPresent(inputFragmentType->
                formGroup.add(inputFragmentType.createFragment(this, formComponent)));
        }

        onFormGroupCreated(formGroup);

        return formGroup;
    }

    // -- COMPACT

    @Override
    protected final Component createCompactFrame() {
        return FrameFragment.COMPACT
                .createComponent(this::createComponentForOutput);
    }

    /**
     * Builds the component to render the model when in COMPACT frame,
     * or when in REGULAR frame rendering the OUTPUT-FORMAT.
     * <p>
     * The (textual) default implementation uses a {@link Label}.
     * However, it may be overridden if required.
     */
    protected Component createComponentForOutput(final String id) {
        return CompactFragment.LABEL.createFragment(id, this, scalarValueId->
            Wkt.label(scalarValueId, obtainOutputFormatModel()));
    }

    // -- HOOKS

    protected Optional<InputFragment> getInputFragmentType() {
        return Optional.empty();
    }

    /**
     * Optional hook, to eg. add additional components (like Blob which adds preview image)
     */
    protected void onFormGroupCreated(final FormGroup formGroup) {};

    protected IValidator<Object> createValidator(final ScalarModel scalarModel) {
        return new IValidator<Object>() {
            private static final long serialVersionUID = 1L;
            private transient IsisAppCommonContext commonContext;

            @Override
            public void validate(final IValidatable<Object> validatable) {
                final ManagedObject proposedAdapter = objectManager().adapt(validatable.getValue());
                final String reasonIfAny = scalarModel.validate(proposedAdapter);
                if (reasonIfAny != null) {
                    final ValidationError error = new ValidationError();
                    error.setMessage(reasonIfAny);
                    validatable.error(error);
                }
            }

            private ObjectManager objectManager() {
                return getCommonContext().getObjectManager();
            }

            private IsisAppCommonContext getCommonContext() {
                return commonContext = CommonContextUtils.computeIfAbsent(commonContext);
            }

        };
    }

    @Override
    protected InlinePromptConfig getInlinePromptConfig() {
        return getFormComponent()!=null
                ? InlinePromptConfig.supportedAndHide(getFormComponent())
                : InlinePromptConfig.notSupported();
    }

    @Override
    protected void onInitializeNotEditable() {
        if(getFormComponent()!=null) {
            //keep inlinePromptLink (if any) enabled
            getFormComponent().setEnabled(false);
        }
        if(getWicketViewerSettings().isReplaceDisabledTagWithReadonlyTag()) {
            Wkt.behaviorAddReplaceDisabledTagWithReadonlyTag(getFormComponent());
        }
        clearTooltip();
    }

    @Override
    protected void onInitializeReadonly(final String disableReason) {
        formComponentEnable(false);
        if(getWicketViewerSettings().isReplaceDisabledTagWithReadonlyTag()) {
            Wkt.behaviorAddReplaceDisabledTagWithReadonlyTag(getFormComponent());
        }
        setTooltip(disableReason);
    }

    @Override
    protected void onInitializeEditable() {
        formComponentEnable(true);
        clearTooltip();
    }

    @Override
    protected void onNotEditable(final String disableReason, final Optional<AjaxRequestTarget> target) {
        formComponentEnable(false);
        setTooltip(disableReason);
        target.ifPresent(this::formComponentAddTo);
    }

    @Override
    protected void onEditable(final Optional<AjaxRequestTarget> target) {
        formComponentEnable(true);
        clearTooltip();
        target.ifPresent(this::formComponentAddTo);
    }

    // -- HELPER

    private void formComponentEnable(final boolean b) {
        if(getFormComponent()!=null) {
            getFormComponent().setEnabled(b);
        }
        if(inlinePromptLink!=null) {
            inlinePromptLink.setEnabled(b);
        }
    }

    private void formComponentAddTo(final AjaxRequestTarget ajax) {
        if(getFormComponent()!=null) {
            ajax.add(getFormComponent());
        }
        if(inlinePromptLink!=null) {
            ajax.add(inlinePromptLink);
        }
    }

    private void setTooltip(final String tooltip) {
        WktTooltips.addTooltip(getFormComponent(), tooltip);
        WktTooltips.addTooltip(inlinePromptLink, tooltip);
    }

    private void clearTooltip() {
        WktTooltips.clearTooltip(getFormComponent());
        WktTooltips.clearTooltip(inlinePromptLink);
    }



}
