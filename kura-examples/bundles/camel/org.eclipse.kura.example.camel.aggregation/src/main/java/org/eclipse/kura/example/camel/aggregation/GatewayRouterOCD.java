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
 * Eurotech
 *******************************************************************************/

package org.eclipse.kura.example.camel.aggregation;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Icon;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition( //
                id = "org.eclipse.kura.example.camel.aggregation.GatewayRouter", //
                name = "Camel Aggregation Example", //
                description = "A Camel example application showing configuration and aggregation.", //
                icon = { //
                                @Icon(resource = "logos/logo.png", size = 32) //
                }

)
@ComponentPropertyType
public @interface GatewayRouterOCD {

        @AttributeDefinition(name = "Minimum", //
                        description = "Lower Limit.", //
                        required = true //
        )
        int minimum() default 0;

        @AttributeDefinition(name = "Maximum", //
                        description = "Upper Limit.", //
                        required = true //
        )
        int maximum() default 40;

}