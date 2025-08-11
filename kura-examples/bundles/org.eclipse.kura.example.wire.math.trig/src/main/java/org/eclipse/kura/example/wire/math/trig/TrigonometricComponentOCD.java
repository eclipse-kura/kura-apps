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

package org.eclipse.kura.example.wire.math.trig;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition(id = "org.eclipse.kura.example.wire.math.trig.TrigonometricComponent", //
        name = "Trigonometric Functions for Wires", //
        description = "A component that applies a trigonometric function to a numeric property converted to double and involving rounding. The result will be emitted as a new property of type double." //
)
@ComponentPropertyType
public @interface TrigonometricComponentOCD {

    @AttributeDefinition(//
            name = "Parameter Name", //
            description = "Specifies the name of the parameter property in the received envelope.", //
            required = true //
    )
    String parameter_name() default "parameter";

    @AttributeDefinition(//
            name = "Trigonometric Function", //
            description = "Specifies the trigonometric function to be executed by the component", //
            required = true, //
            options = { //
                    @Option(label = "SIN", value = "SIN"), //
                    @Option(label = "COS", value = "COS"), //
                    @Option(label = "TAN", value = "TAN"), //
                    @Option(label = "ASIN", value = "ASIN"), //
                    @Option(label = "ACOS", value = "ACOS"), //
                    @Option(label = "ATAN", value = "ATAN") //
            })
    String trigonometric_function() default "SIN";

    @AttributeDefinition(//
            name = "Result Name", //
            description = "Specifies the name of the result property in the emitted envelope.", //
            required = true //
    )
    String result_name() default "result";

    @AttributeDefinition(//
            name = "Emit Received Properties", //
            description = "Specifies whether received properties should be included in the emitted envelope or not.", //
            required = true //
    )
    boolean emit_received_properties() default false;
}
