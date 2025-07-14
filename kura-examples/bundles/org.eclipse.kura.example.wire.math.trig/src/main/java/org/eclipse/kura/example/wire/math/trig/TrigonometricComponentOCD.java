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

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(id = "org.eclipse.kura.wire.TrigonometricFunctions", name = "Trigonometric Functions for Wires", description = "A component that applies a trigonometric function to a numeric property converted to double and involving rounding. The result will be emitted as a new property of type double.")
public @interface TrigonometricComponentOCD {

    @AttributeDefinition(name = "Parameter Name", //
            type = AttributeType.STRING, //
            required = true, //
            cardinality = 0, //
            defaultValue = "parameter", //
            description = "Specifies the name of the parameter property in the received envelope." //
    )
    String parameter_name() default "parameter";

    @AttributeDefinition(name = "Function", //
            type = AttributeType.STRING, //
            required = true, //
            cardinality = 0, //
            description = "Specifies the trigonometric function to be executed by the component", //
            options = {
                    @Option(label = "SIN", value = "SIN"),
                    @Option(label = "COS", value = "COS"),
                    @Option(label = "TAN", value = "TAN"),
                    @Option(label = "ASIN", value = "ASIN"),
                    @Option(label = "ACOS", value = "ACOS"),
                    @Option(label = "ATAN", value = "ATAN")
            } //
    )
    String trigonometric_function() default "SIN";

    @AttributeDefinition(name = "Result Name", //
            type = AttributeType.STRING, //
            required = true, //
            cardinality = 0, //
            description = "Specifies the name of the result property in emitted envelope." //
    )
    String result_name() default "result";

    @AttributeDefinition(name = "Emit Received Properties", //
            type = AttributeType.BOOLEAN, //
            required = true, //
            cardinality = 0, //
            description = "Specifies whether received properties should be included in the emitted envelope or not." //
    )
    boolean emit_received_properties() default false;
}
