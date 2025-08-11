package org.eclipse.kura.wire.devel.sink;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition(id = "org.eclipse.kura.wire.devel.sink.Sink", //
                name = "Sink", //
                description = "A no-op receiver component." //
)
@ComponentPropertyType
public @interface SinkOCD {

        @AttributeDefinition(//
                        name = "Measure Timings", //
                        description = "If set to true, the component will print on the log the time interval in milliseconds between the last two received envelopes.", //
                        required = false, //
                        min = "0" //
        )
        boolean measure_timings() default true;
}