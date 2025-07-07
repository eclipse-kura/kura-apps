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
package org.eclipse.kura.example.wire;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

// allow using _ in method names, it is needed for having ids containing '.'
@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition(id = "org.eclipse.kura.example.wire.WireComponentExample", //
        name = "WireComponentExample", //
        description = "An example of wire configurable component implementation. This component get a WireAsset as input and extract the value from the channel whose name is equal to the one specified in the options. Then emits a message using the channel extracted." //
)

@ComponentPropertyType
public @interface WireComponentExampleOCD {

    @AttributeDefinition(name = "Channel Filter Name", //
            defaultValue = "Channel-1", //
            description = "String used as filter to select the correct channel from the input wire records.", //
            required = true, //
            cardinality = 0 //
    )
    public String channel_filter_name();

}