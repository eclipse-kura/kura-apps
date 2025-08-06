package org.eclipse.kura.example.wire.math.singleport.sqrt;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition(id = "org.eclipse.kura.example.wire.math.singleport.sqrt.SqrtComponent", //
        name = "Square Root", //
        description = "A wire component that performs the square root of a configurable numeric property received in input in the component. The result will be emitted as a new property of type double." //
)
@ComponentPropertyType
public @interface SqrtComponentOCD {

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
            name = "Emit Received Properties", //
            description = "Specifies whether received properties should be included in the emitted envelope or not.", //
            required = true //
    )
    boolean emit_received_properties() default false;
}