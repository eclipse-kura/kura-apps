/*******************************************************************************
 * Copyright (c) 2025, 2026 Eurotech and/or its affiliates and others
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

package org.eclipse.kura.example.gpio;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition( //
                id = "org.eclipse.kura.example.gpio.GpioComponent", //
                name = "GPIO Configuration", //
                description = "Example of a GPIO Configuring Application." //

)
@ComponentPropertyType
public @interface GpioComponentOCD {

        @AttributeDefinition(name = "Gpio Service Pid", //
                        description = "The Kura GPIO service to be used to interact with gpios.", //
                        required = true //
        )
        String gpio_service_pid() default "org.eclipse.kura.gpio.GPIOService";

        @AttributeDefinition(name = "Gpio Input Read Mode", //
                        description = "Specifies how input pin value is obtained. If set to PIN_STATUS_LISTENER, the pin value will be printed on the log on change, if set to POLLING, the pin value will be readed and printed on the log periodically.", //
                        required = true, //
                        options = { //
                                        @Option(label = "PIN_STATUS_LISTENER", value = "PIN_STATUS_LISTENER"), //
                                        @Option(label = "POLLING", value = "POLLING") //
                        })
        String gpio_input_read_mode() default "PIN_STATUS_LISTENER";

        @AttributeDefinition(name = "Gpio Pins", //
                        description = "List of GPIO pins expressed as pin number (i.e. 1022), pin name (i.e. DOUT1, DIN1, ...) or controller:line (i.e. 0:24).", //
                        required = false, //
                        cardinality = 5 //
        )
        String[] gpio_pins() default {};

        @AttributeDefinition(name = "Gpio Directions", //
                        description = "Pin directions", //
                        required = false, //
                        cardinality = 5, //
                        options = { //
                                        @Option(label = "Only input", value = "0"), //
                                        @Option(label = "Only output", value = "1"), //
                                        @Option(label = "Both, init input", value = "2"), //
                                        @Option(label = "Both, init output", value = "3") //
                        })
        int[] gpio_directions() default { 3, 3, 3, 3, 3 };

        @AttributeDefinition(name = "Gpio Modes", //
                        description = "Pin working mode", //
                        required = false, //
                        cardinality = 5, //
                        options = { //
                                        @Option(label = "Default", value = "-1"), //
                                        @Option(label = "Input with Pull Down", value = "2"), //
                                        @Option(label = "Input with Pull Up", value = "1"), //
                                        @Option(label = "Open Drain Output", value = "8"), //
                                        @Option(label = "Push-Pull Output", value = "4") //
                        })
        int[] gpio_modes() default { -1, -1, -1, -1, -1 };

        @AttributeDefinition(name = "Gpio Triggers", //
                        description = "Input triggering mode", //
                        required = false, //
                        cardinality = 5, //
                        options = { //
                                        @Option(label = "Default", value = "-1"), //
                                        @Option(label = "No trigger", value = "0"), //
                                        @Option(label = "Rising edge", value = "2"), //
                                        @Option(label = "Both edges", value = "3"), //
                                        @Option(label = "Falling edge", value = "1") //
                        })
        int[] gpio_triggers() default { -1, -1, -1, -1, -1 };
}