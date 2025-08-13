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
 ******************************************************************************/
package org.eclipse.kura.wire.devel.delay;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition(id = "org.eclipse.kura.wire.devel.delay.Delay", //
                name = "Delay", //
                description = "A wire component that introduces a configurable delay." //
)
@ComponentPropertyType
public @interface DelayOCD {

        @AttributeDefinition(//
                        name = "Delay Average", //
                        description = "The average delay in milliseconds.", //
                        required = true, //
                        min = "0" //
        )
        int delay_average() default 1000;

        @AttributeDefinition(//
                        name = "Delay Standard Deviation", //
                        description = "The standard deviation of the delay in milliseconds.", //
                        required = true, //
                        min = "0" //
        )
        int delay_standard_deviation() default 100;
}