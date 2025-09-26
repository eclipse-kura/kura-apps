/*******************************************************************************
 * Copyright (c) 2018, 2025 Eurotech and/or its affiliates and others
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
