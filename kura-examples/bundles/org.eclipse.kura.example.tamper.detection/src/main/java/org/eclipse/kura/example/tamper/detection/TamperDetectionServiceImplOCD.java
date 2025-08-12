/*******************************************************************************
 * Copyright (c) 2025 Eurotech and/or its affiliates and others
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

package org.eclipse.kura.example.tamper.detection;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition( //
        id = "org.eclipse.kura.example.tamper.detection.TamperDetectionServiceImpl", //
        name = "Tamper Detection", //
        description = "This service emulates a TamperDetectionService." //

)
@ComponentPropertyType
public @interface TamperDetectionServiceImplOCD {

    @AttributeDefinition(name = "Enabled", //
            description = "Specifies if the tamper detection service is enabled or not.", //
            required = true //
    )
    boolean enabled() default true;

    @AttributeDefinition(name = "Device Tamper Status", //
            description = "Specifies the current device status. If set to true to simulates a device tamper event, if set to false clears the tamper status.", //
            required = true //
    )
    boolean deviceTamperStatus() default false;
}