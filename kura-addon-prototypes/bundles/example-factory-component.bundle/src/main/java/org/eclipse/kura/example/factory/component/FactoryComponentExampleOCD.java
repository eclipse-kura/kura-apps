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
package org.eclipse.kura.example.factory.component;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

// allow using _ in method names, it is needed for having ids containing '.'
@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition(id = "org.eclipse.kura.example.factory.FactoryComponentExample", //
        name = "FactoryComponentExample", //
        description = "An example of factory configurable component implementation." //
)
@ComponentPropertyType
public @interface FactoryComponentExampleOCD {

    @AttributeDefinition(name = "TCP Port", //
            description = "Port on which the component will respond.", //
            required = true, //
            type = AttributeType.INTEGER, //
            cardinality = 0 //
    )
    public int tcp_port()

    default 1234;

    @AttributeDefinition(name = "Available messages", //
            description = "Comma separated list of request and answers. Each line must be of type 'request:answer' (eg: 'Hello:Hi, I'm an example factory component').|TextArea", //
            required = true, //
            cardinality = 0 //
    )

    public String available_messages() default "Hello:Hi";

    @AttributeDefinition(name = "Case Sensitive Enabled", //
            description = "Define if the request must be case sensitive or not.", //
            required = true, //
            type = AttributeType.BOOLEAN, //
            cardinality = 0 //
    )
    public boolean case_sensitive() default false;

}