/*******************************************************************************
 * Copyright (c) 2024, 2025 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Eurotech
 *******************************************************************************/
package org.eclipse.kura.example.container.signature.validation;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition( //
                id = "org.eclipse.kura.example.container.signature.validation.DummyContainerSignatureValidationService", //
                name = "Dummy Container Signature Validation Service", //
                description = "This is an example implementation of a service implementing a Container Signature Validation Service. The main purpose of this service is testing and serving as reference for future implementations." //
)
@ComponentPropertyType
public @interface DummyContainerSignatureValidationServiceOCD {

        @AttributeDefinition( //
                        name = "Set signature validation outcome", //
                        description = "Set the outcome for container signature validations performed by this service. Supported format: imageName:imageTag@digest. Example: hello-world:latest@sha256:e2fc4e...|TextArea", //
                        defaultValue = "")
        String set_signature_validation_outcome();
}
