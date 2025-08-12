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

package org.eclipse.kura.example.gpio.led;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition( //
        id = "org.eclipse.kura.example.gpio.led.LedExample", //
        name = "Gpio Led Example", //
        description = "This example allows to configure and manage the state of a LED connected to a configurable GPIO pin." //

)
@ComponentPropertyType
public @interface LedExampleOCD {

    @AttributeDefinition(name = "Configure Pin", //
            description = "Configure the Pin to use.", //
            required = true, //
            min = "0" //
    )
    int configure_pin() default 6;

    @AttributeDefinition(name = "Led State", //
            description = "Switches the selected GPIO port state.", //
            required = true //
    )
    boolean led_state() default false;
}