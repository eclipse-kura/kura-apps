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
package org.eclipse.kura.example.wire.logic.multiport.provider;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition(id = "org.eclipse.kura.wire.LogicalOperators", //
        name = "Logical Operators for Wires", //
        description = "A component that applies a logical operator to one or two boolean properties received from the first and, optionally, the second input port, emitting the result. For binary operators, the operation is performed when a new envelope is received on a port, using the envelope cached by the other port; alternatively, new envelopes must be received on both ports. For the NOT operator, the operand must be received from the first port." //
)
@ComponentPropertyType
public @interface LogicalComponentOCD {

    @AttributeDefinition(//
            name = "Logical operator", //
            description = "Specifies the logical operation to be executed by the component", //
            required = true, //
            options = { //
                    @Option(label = "AND", value = "AND"), //
                    @Option(label = "OR", value = "OR"), //
                    @Option(label = "NOR", value = "NOR"), //
                    @Option(label = "NAND", value = "NAND"), //
                    @Option(label = "XOR", value = "XOR"), //
                    @Option(label = "NOT", value = "NOT") //
            })
    String logical_operator() default "AND";

    @AttributeDefinition(//
            name = "First Operand", //
            description = "Specifies the name of the operand property in the envelope received on the first port.", //
            required = true //
    )
    String operand_name_1() default "operand";

    @AttributeDefinition(//
            name = "Second Operand", //
            description = "Specifies the name of the operand property in the envelope received on the second port; ignored by the NOT operator.", //
            required = true //
    )
    String operand_name_2() default "operand";

    @AttributeDefinition(//
            name = "Result Name", //
            description = "Specifies the name of the result property in the emitted envelope.", //
            required = true //
    )
    String result_name() default "result";

    @AttributeDefinition(//
            name = "Barrier", //
            description = "Specifies if the component should use a barrier for input ports or perform caching of messages; ignored by the NOT operator", //
            required = true //
    )
    boolean barrier() default false;
}
