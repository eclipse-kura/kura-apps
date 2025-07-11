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
package org.eclipse.kura.example.can;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition(name = "CAN Example", //
        description = "Can example application.", //
        id = "org.eclipse.kura.example.can.CanSocketExample")
@ComponentPropertyType
public @interface CanSocketExampleOCD {

    @AttributeDefinition(name = "Can name", //
            description = "CAN socket Name.", //
            defaultValue = "can0")
    String can_name();

    @AttributeDefinition(name = "Can identifier", //
            description = "Can device identifier.", //
            defaultValue = "0", //
            required = false)
    int can_identifier();

    @AttributeDefinition(name = "Master", //
            description = "Whether this device will act as a master or a slave", //
            defaultValue = "true")
    boolean master();
}
