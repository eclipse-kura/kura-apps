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

package org.eclipse.kura.example.wire.math.multiport.sum;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * A wire component that performs the sum of the values of two properties
 * contained in two envelopes received on different ports.
 */
@ObjectClassDefinition( //
        id = "org.eclipse.kura.example.wire.math.multiport.sum.SumComponent", //
        name = "Sum", //
        description = "A wire component that performs the sum of the values of two properties contained in two envelopes received on different ports." //
)
public @interface SumComponentOCD {

    @AttributeDefinition(name = "operand.name.1", //
            description = "Specifies the name of the operand property in the envelope received on the first port.", //
            cardinality = 0, //
            required = true //
    )
    String operand_name_1() default "operand";

    @AttributeDefinition(name = "operand.name.2", //
            description = "Specifies the name of the operand property in the envelope received on the second port.", //
            cardinality = 0, //
            required = true //
    )
    String operand_name_2() default "operand";

    @AttributeDefinition(name = "result.name", //
            description = "Specifies the name of the result property in emitted envelope.", //
            cardinality = 0, //
            required = true //
    )
    String result_name() default "result";

    @AttributeDefinition(name = "barrier", //
            description = "Specifies if the component should use a barrier for input ports or perform caching of messages", //
            cardinality = 0, //
            required = true //
    )
    boolean barrier() default true;
}
