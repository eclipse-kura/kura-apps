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

package org.eclipse.kura.raspberrypi.sensehat.example;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Icon;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition( //
                id = "org.eclipse.kura.raspberrypi.sensehat.example.SenseHatExample", //
                name = "SenseHat", //
                description = "This bundle is an interface to the Raspberry Sense Hat.", //
                icon = @Icon(resource = "logos/astro-pi-hat.png", size = 32) //
)
@interface SenseHatExampleOCD {

        @AttributeDefinition( //
                        name = "Imu Accelerometer Enable", //
                        required = true, //
                        description = "Enable accelerometer LSM9DS1 IMU sensor." //
        )
        boolean imu_accelerometer_enable() default false;

        @AttributeDefinition( //
                        name = "Imu Gyroscope Enable", //
                        required = true, //
                        description = "Enable gyroscope LSM9DS1 IMU sensor." //
        )
        boolean imu_gyroscope_enable() default false;

        @AttributeDefinition( //
                        name = "Imu Compass Enable", //
                        required = true, //
                        description = "Enable compass/magnetometer LSM9DS1 IMU sensor." //
        )
        boolean imu_compass_enable() default false;

        @AttributeDefinition( //
                        name = "Imu Sample Number", //
                        required = true, //
                        description = "Set the number of samples that the IMU sensor has to retrieve before returning the result." //
        )
        int imu_sample_number() default 20;

        @AttributeDefinition( //
                        name = "Pressure Enable", //
                        required = true, //
                        description = "Enable LPS25H Pressure sensor." //
        )
        boolean pressure_enable() default false;

        @AttributeDefinition( //
                        name = "Humidity Enable", //
                        required = true, //
                        description = "Enable HTS221 Humidity sensor." //
        )
        boolean humidity_enable() default false;

        @AttributeDefinition( //
                        name = "Lcd Screen Enable", //
                        required = true, //
                        description = "Enable lcd screen." //
        )
        boolean lcd_screen_enabled() default false;

        @AttributeDefinition( //
                        name = "Screen Rotation", //
                        required = true, //
                        description = "Set the screen rotation.", //
                        options = { //
                                        @Option(label = "0", value = "0"), //
                                        @Option(label = "90", value = "90"), //
                                        @Option(label = "180", value = "180"), //
                                        @Option(label = "270", value = "270") //
                        } //
        )
        int screen_rotation() default 0;

        @AttributeDefinition( //
                        name = "Screen Message", //
                        required = true, //
                        description = "Message to be written on screen." //
        )
        String screen_message() default "Hello!";

        @AttributeDefinition( //
                        name = "Screen Text Color", //
                        required = true, //
                        description = "Set the color of the text.", //
                        options = { //
                                        @Option(label = "RED", value = "RED"), //
                                        @Option(label = "ORANGE", value = "ORANGE"), //
                                        @Option(label = "YELLOW", value = "YELLOW"), //
                                        @Option(label = "GREEN", value = "GREEN"), //
                                        @Option(label = "BLUE", value = "BLUE"), //
                                        @Option(label = "PURPLE", value = "PURPLE"), //
                                        @Option(label = "VIOLET", value = "VIOLET"), //
                                        @Option(label = "WHITE", value = "WHITE"), //
                                        @Option(label = "BLACK", value = "BLACK") //
                        } //
        )
        String screen_text_color() default "ORANGE";

        @AttributeDefinition( //
                        name = "Stick Enable", //
                        required = true, //
                        description = "Enable stick." //
        )
        boolean stick_enable() default false;
}
