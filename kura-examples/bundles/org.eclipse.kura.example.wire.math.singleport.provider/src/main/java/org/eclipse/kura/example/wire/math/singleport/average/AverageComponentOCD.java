package org.eclipse.kura.example.wire.math.singleport.average;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition(id = "org.eclipse.kura.example.wire.math.singleport.average.AverageComponent", //
        name = "Average", //
        description = "A wire component that finds the average value of a numeric property in a fixed size buffer containing the last received values, emitting the result. For comparison, values are promoted to double which is also the type of the result." //
)
@ComponentPropertyType
public @interface AverageComponentOCD {

    @AttributeDefinition(//
            name = "Operand Name", //
            description = "Specifies the name of the operand property in the received envelope.", //
            required = true //
    )
    String operand_name() default "operand";

    @AttributeDefinition(//
            name = "Result Name", //
            description = "Specifies the name of the result property in emitted envelope.", //
            required = true //
    )
    String result_name() default "result";

    @AttributeDefinition(//
            name = "Window Size", //
            description = "Specifies the window size.", //
            min = "1", //
            required = true //
    )
    int window_size() default 10;

    @AttributeDefinition(//
            name = "Emit Received Properties", //
            description = "Specifies whether received properties should be included in the emitted envelope or not.", //
            required = true //
    )
    boolean emit_received_properties() default false;

}
