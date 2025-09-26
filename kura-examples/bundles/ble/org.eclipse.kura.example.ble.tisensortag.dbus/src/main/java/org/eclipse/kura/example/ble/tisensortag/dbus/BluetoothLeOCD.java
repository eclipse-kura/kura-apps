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
package org.eclipse.kura.example.ble.tisensortag.dbus;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition(name = "BluetoothLe", //
        description = "Bluetooth Low Energy demo on a TI SensorTag.", //
        id = "org.eclipse.kura.example.ble.tisensortag.dbus.BluetoothLe" //
)
@ComponentPropertyType
public @interface BluetoothLeOCD {

    @AttributeDefinition(name = "Cloud Publisher Target Filter", //
            description = "Specifies, as an OSGi target filter, the pid of the Cloud Publisher used to publish messages to the cloud platform.", //
            defaultValue = "(kura.service.pid=changeme)")
    String cloud_publisher_target_filter();

    @AttributeDefinition(name = "scan_enable", //
            description = "Enable scan for TI SensorTags.", //
            defaultValue = "false")
    boolean scan_enable();

    @AttributeDefinition(name = "scan_time", //
            description = "Scan for devices duration in seconds.", //
            defaultValue = "5")
    int scan_time();

    @AttributeDefinition(name = "period", //
            description = "Period in seconds between 2 publishes. It must be greater than scan_time.", //
            defaultValue = "120")
    int period();

    @AttributeDefinition(name = "Enable Thermometer", //
            description = "Enable temperature sensor.", //
            defaultValue = "false")
    boolean enable_thermometer();

    @AttributeDefinition(name = "Enable Accelerometer", //
            description = "Enable accelerometer sensor.", //
            defaultValue = "false")
    boolean enable_accelerometer();

    @AttributeDefinition(name = "Enable Hygrometer", //
            description = "Enable humidity sensor.", //
            defaultValue = "false")
    boolean enable_hygrometer();

    @AttributeDefinition(name = "Enable Magnetometer", //
            description = "Enable magnetometer sensor.", //
            defaultValue = "false")
    boolean enable_magnetometer();

    @AttributeDefinition(name = "Enable Barometer", //
            description = "Enable pressure sensor.", //
            defaultValue = "false")
    boolean enable_barometer();

    @AttributeDefinition(name = "Enable Gyroscope", //
            description = "Enable gyroscope sensor.", //
            defaultValue = "false")
    boolean enable_gyroscope();

    @AttributeDefinition(name = "Enable Luxometer", //
            description = "Enable light sensor (CC2650 only).", //
            defaultValue = "false")
    boolean enable_luxometer();

    @AttributeDefinition(name = "Enable Buttons", //
            description = "Enable buttons.", //
            defaultValue = "false")
    boolean enable_buttons();

    @AttributeDefinition(name = "Switch On Red Led", //
            description = "Switch on the red led.", //
            defaultValue = "false")
    boolean switch_on_red_led();

    @AttributeDefinition(name = "Switch On Green Led", //
            description = "Switch on the green led.", //
            defaultValue = "false")
    boolean switch_on_green_led();

    @AttributeDefinition(name = "Switch On Buzzer", //
            description = "Switch on the buzzer.", //
            defaultValue = "false")
    boolean switch_on_buzzer();

    @AttributeDefinition(name = "Discover Services And Characteristics", //
            description = "Perform a discovery of GATT services and characteristics.", //
            defaultValue = "false")
    boolean discover_services_and_characteristics();

    @AttributeDefinition(name = "iname", //
            description = "Name of bluetooth adapter.", //
            defaultValue = "hci0")
    String iname();
}
