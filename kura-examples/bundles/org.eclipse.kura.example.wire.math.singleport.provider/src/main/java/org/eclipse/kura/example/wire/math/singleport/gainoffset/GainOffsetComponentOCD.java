package org.eclipse.kura.example.wire.math.singleport.gainoffset;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition(id = "org.eclipse.kura.example.wire.math.singleport.gainoffset.GainOffsetComponent", //
        name = "Gain Offset", //
        description = "A wire component that applies a gain and an offset to a configurable set of input properties." //
)
@ComponentPropertyType
public @interface GainOffsetComponentOCD {

    @AttributeDefinition(name = "Configuration", //
            description = "Specifies the properties on which apply a gain and offset. Must be a list of items in the form <property name>|<gain>|<offset> separated by ; or by a new line. The offset parameter can be omitted, in this case it will be assumed as 0.|TextArea", //
            required = true, //
            cardinality = 0 //
    )
    public String configuration() default "toBeMultipliedByTwo | 2;toBeMultipliedBy3AndIncreasedBy1 | 3 | 1";

    @AttributeDefinition(//
            name = "Emit Received Properties", //
            description = "Specifies whether received properties should be included in the emitted envelope or not.", //
            required = true //
    )
    boolean emit_received_properties() default false;

}