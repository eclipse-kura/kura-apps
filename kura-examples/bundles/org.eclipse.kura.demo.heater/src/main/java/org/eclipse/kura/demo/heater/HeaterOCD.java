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
package org.eclipse.kura.demo.heater;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition(name = "Heater", //
        description = "This is a simulator for an heater gateway application. Its configuration options will be passed down the smart heater.", //
        id = "org.eclipse.kura.demo.heater.Heater" //

)
@ComponentPropertyType
public @interface HeaterOCD {

    @AttributeDefinition(name = "CloudPublisher Target Filter", //
            description = "Specifies, as an OSGi target filter, the pid of the Cloud Publisher used to publish messages to the cloud platform.", //
            defaultValue = "(kura.service.pid=changeme)")
    String cloudpublisher_target_filter();

    @AttributeDefinition(name = "mode", //
            description = "Operating mode for the heater. If operatng mode is Vacation, set point is automatiaclly set to 6.0C.", //
            defaultValue = "Program", //
            options = {
                    @Option(label = "Program", value = "Program"),
                    @Option(label = "Manual", value = "Manual"),
                    @Option(label = "Vacation", value = "Vacation")
            })
    String mode();

    @AttributeDefinition(name = "Program Start Time", //
            description = "Start time for the heating cycle with the operating mode is Program.", //
            defaultValue = "06:00", //
            required = false)
    String program_start_time();

    @AttributeDefinition(name = "Program Stop Time", //
            description = "Stop time for the heating cycle with the operating mode is Program.", //
            defaultValue = "22:00", //
            required = false)
    String program_stop_time();

    @AttributeDefinition(name = "Program Set Point", //
            description = "Temperature Set Point in Celsius for the heating cycle with the operating mode is Program.", //
            defaultValue = "20.5", //
            min = "5.0", //
            max = "40.0", //
            required = false)
    float program_set_point();

    @AttributeDefinition(name = "Manual Set Point", //
            description = "Temperature Set Point in Celsius for the heating cycle with the operating mode is Manual.", //
            defaultValue = "15.0", //
            min = "5.0", //
            max = "40.0", //
            required = false)
    float manual_set_point();

    @AttributeDefinition(name = "Temperature Initial", //
            description = "Initial value for the temperature metric.", //
            defaultValue = "10")
    float temperature_initial();

    @AttributeDefinition(name = "Temperature Increment", //
            description = "Increment value for the temperature metric.", //
            defaultValue = "0.25")
    float temperature_increment();

    @AttributeDefinition(name = "Publish Rate", //
            description = "Default message publishing rate in seconds (min 1).", //
            defaultValue = "2", //
            min = "1")
    int publish_rate();
}
