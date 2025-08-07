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

package org.eclipse.kura.example.serial.publisher;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Icon;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition( //
        id = "org.eclipse.kura.example.serial.publisher.ExampleSerialPublisher", //
        name = "ExampleSerialPublisher", //
        description = "Example of a Configuring Kura Application publishing data read from the serial port.", //
        icon = { //
                @Icon(resource = "http://s3.amazonaws.com/kura-resources/application/icon/applications-other.png", //
                        size = 32) //
        } //

)
@ComponentPropertyType
public @interface ExampleSerialPublisherOCD {

    @AttributeDefinition(name = "CloudPublisher Target Filter", //
            description = "Specifies, as an OSGi target filter, the pid of the Cloud Publisher used to publish messages to the cloud platform.", //
            required = true //
    )
    String CloudPublisher_target() default "(kura.service.pid=changeme)";

    @AttributeDefinition(name = "CloudSubscriber Target Filter", //
            description = "Specifies, as an OSGi target filter, the pid of the Cloud Subscriber used to receive messages from the cloud platform.", //
            required = true //
    )
    String CloudSubscriber_target() default "(kura.service.pid=changeme)";

    @AttributeDefinition(name = "serial.device", //
            description = "Name of the serial device (e.g. /dev/ttyS0, /dev/ttyACM0, /dev/ttyUSB0).", //
            required = false //
    )
    String serial_device() default "";

    @AttributeDefinition(name = "serial.baudrate", //
            description = "Baudrate.", //
            required = true, //
            options = { //
                    @Option(label = "9600", value = "9600"), //
                    @Option(label = "19200", value = "19200"), //
                    @Option(label = "38400", value = "38400"), //
                    @Option(label = "57600", value = "57600"), //
                    @Option(label = "115200", value = "115200") //
            } //
    )
    String serial_baudrate() default "9600";

    @AttributeDefinition(name = "serial.data-bits", //
            description = "Data bits.", //
            required = true, //
            options = { //
                    @Option(label = "7", value = "7"), //
                    @Option(label = "8", value = "8") //
            } //
    )
    String serial_data_bits() default "8";

    @AttributeDefinition(name = "serial.parity", //
            description = "Parity.", //
            required = true, //
            options = { //
                    @Option(label = "none", value = "none"), //
                    @Option(label = "even", value = "even"), //
                    @Option(label = "odd", value = "odd") //
            } //
    )
    String serial_parity() default "none";

    @AttributeDefinition(name = "serial.stop-bits", //
            description = "Stop bits.", //
            required = true, //
            options = { //
                    @Option(label = "1", value = "1"), //
                    @Option(label = "2", value = "2") //
            } //
    )
    String serial_stop_bits() default "1";

    @AttributeDefinition(name = "serial.echo", //
            description = "Wheter the device echoes received characters.", //
            required = true //
    )
    boolean serial_echo() default false;

    /*
     * This option is never used in the example, but it is left here for future use.
     * 
     * @AttributeDefinition(name = "serial.cloud-echo", //
     * description = "Wheter the Cloud echoes received characters.", //
     * required = true //
     * )
     * boolean serial_cloud_echo() default false;
     */
}