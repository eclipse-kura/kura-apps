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
package org.eclipse.kura.wire.devel.driver.dummy;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition(id = "org.eclipse.kura.wire.devel.driver.dummy.DummyDriver", //
        name = "DummyDriver", //
        description = "A dummy driver." //
)
@ComponentPropertyType
public @interface DummyDriverOCD {

    @AttributeDefinition(//
            name = "Connection Delay", //
            description = "If set, the driver will simulate a connection delay by sleeping for connection.delay milliseconds", //
            required = false, //
            min = "0" //
    )
    Integer connection_delay() default 0;

    @AttributeDefinition(//
            name = "Channel Descriptor Issues", //
            description = "Allows to simulate channel descriptor issues", //
            required = true, //
            options = { @Option(label = "NONE", value = "NONE"), //
                    @Option(label = "THROW", value = "THROW"), //
                    @Option(label = "RETURN_NULL", value = "RETURN_NULL"), //
                    @Option(label = "RETURN_INVALID_OBJECT", value = "RETURN_INVALID_OBJECT") } //
    )
    String channel_descriptor_issues() default "NONE";

    @AttributeDefinition(//
            name = "Prepared Read Issues", //
            description = "Allows to simulate prepared read issues", //
            required = true, //
            options = { @Option(label = "NONE", value = "NONE"), //
                    @Option(label = "THROW", value = "THROW"), //
                    @Option(label = "RETURN_NULL", value = "RETURN_NULL") } //
    )
    String prepared_read_issues() default "NONE";

    @AttributeDefinition(//
            name = "Connection Issues", //
            description = "Allows to simulate connection issues", //
            required = true, //
            options = { @Option(label = "NONE", value = "NONE"), //
                    @Option(label = "THROW", value = "THROW") } //
    )
    String connection_issues() default "NONE";
}