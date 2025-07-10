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
package org.eclipse.kura.demo.modbus;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition(name = "ModbusExample", //
        description = "Modbus protocol example application.", //
        id = "org.eclipse.kura.demo.modbus.ModbusExample" //
)
@ComponentPropertyType
public @interface ModbusExampleOCD {

    @AttributeDefinition(name = "Cloud Publisher Target Filter", //
            description = "Specifies, as an OSGi target filter, the pid of the Cloud Publisher used to publish messages to the cloud platform.", //
            defaultValue = "(kura.service.pid=changeme)")
    String cloud_publisher_target_filter();

    @AttributeDefinition(name = "protocol", //
            description = "Type of connection : Serial Mode (RS232), TCP-RTU for TCP-encapsulated Modbus, or ModbusTCP/IP for the full TCP/IP implementation.", //
            defaultValue = "RS232", //
            required = false, //
            options = {
                    @Option(label = "RS-232", value = "RS232"),
                    @Option(label = "TCP-RTU", value = "TCP-RTU"),
                    @Option(label = "TCP/IP", value = "TCP/IP")
            })
    String protocol();

    @AttributeDefinition(name = "Serial port", //
            description = "Serial Port Name.", //
            defaultValue = "/dev/ttyUSB0", //
            required = false)
    String serial_port();

    @AttributeDefinition(name = "Serial baudrate", //
            description = "The baud rate of the serial connection.", //
            defaultValue = "9600", //
            required = false, //
            options = {
                    @Option(label = "110", value = "110"),
                    @Option(label = "300", value = "300"),
                    @Option(label = "600", value = "600"),
                    @Option(label = "1200", value = "1200"),
                    @Option(label = "2400", value = "2400"),
                    @Option(label = "4800", value = "4800"),
                    @Option(label = "9600", value = "9600"),
                    @Option(label = "14400", value = "14400"),
                    @Option(label = "19200", value = "19200"),
                    @Option(label = "28800", value = "28800"),
                    @Option(label = "38400", value = "38400"),
                    @Option(label = "56000", value = "56000"),
                    @Option(label = "57600", value = "57600"),
                    @Option(label = "115200", value = "115200"),
                    @Option(label = "128000", value = "128000"),
                    @Option(label = "153600", value = "153600"),
                    @Option(label = "230400", value = "230400"),
                    @Option(label = "256000", value = "256000"),
                    @Option(label = "460800", value = "460800"),
                    @Option(label = "921600", value = "921600")
            })
    String serial_baudrate();

    @AttributeDefinition(name = "Serial data bits", //
            description = "The number of bits per word or the 'data bits' for the serial connection.", //
            defaultValue = "8", //
            required = false, //
            options = {
                    @Option(label = "5", value = "5"),
                    @Option(label = "6", value = "6"),
                    @Option(label = "7", value = "7"),
                    @Option(label = "8", value = "8")
            })
    String serial_data_bits();

    @AttributeDefinition(name = "Serial stop bits", //
            description = "The stop bits for the serial connection.", //
            defaultValue = "1", //
            required = false, //
            options = {
                    @Option(label = "1", value = "1"),
                    @Option(label = "2", value = "2"),
                    @Option(label = "1.5", value = "3")
            })
    String serial_stop_bits();

    @AttributeDefinition(name = "Serial parity", //
            description = "The parity for the serial connection.", //
            defaultValue = "0", //
            required = false, //
            options = {
                    @Option(label = "None", value = "0"),
                    @Option(label = "Odd", value = "1"),
                    @Option(label = "Even", value = "2"),
                    @Option(label = "Mark", value = "3"),
                    @Option(label = "Space", value = "4")
            })
    String serial_parity();

    @AttributeDefinition(name = "Slave Address", //
            description = "The modbus address of the device to be queried.", //
            defaultValue = "1")
    String slave_address();

    @AttributeDefinition(name = "IP Address", //
            description = "IP address of Modbus server (TCP mode only).", //
            defaultValue = "", //
            required = false)
    String ip_address();

    @AttributeDefinition(name = "Tcp port", //
            description = "TCP port for Modbus TCP (usually 502).", //
            defaultValue = "502", //
            min = "1", //
            max = "65535", //
            required = false)
    int tcp_port();

    @AttributeDefinition(name = "Poll Interval", //
            description = "Interval in milliseconds between 2 polls of the modbus device.", //
            defaultValue = "1000")
    int poll_interval();

    @AttributeDefinition(name = "Publish Interval", //
            description = "Interval in seconds between 2 publishes.", //
            defaultValue = "10")
    int publish_interval();

    @AttributeDefinition(name = "Input Address", //
            description = "Address in decimal of the discrete input to read. Leave 0 if no input.", //
            defaultValue = "0", //
            min = "0", //
            required = false)
    int input_address();

    @AttributeDefinition(name = "Register Address", //
            description = "Address in decimal of the register to read. Leave 0 if no input.", //
            defaultValue = "0", //
            min = "0", //
            required = false)
    int register_address();
}
