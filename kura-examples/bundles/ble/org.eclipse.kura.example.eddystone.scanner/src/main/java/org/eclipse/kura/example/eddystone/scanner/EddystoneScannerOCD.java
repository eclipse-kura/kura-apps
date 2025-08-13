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

package org.eclipse.kura.example.eddystone.scanner;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Icon;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition( //
        id = "org.eclipse.kura.example.eddystone.scanner.EddystoneScanner", //
        name = "Eddystone Scanner", //
        description = "The Eddystone Scanner detects UID and URL frame types and publishes on the cloud the detected beacons.", //
        icon = { //
                @Icon(resource = "http://s3.amazonaws.com/kura-resources/application/icon/eddystone_logo.png", //
                        size = 32) //
        } //

)
@ComponentPropertyType
public @interface EddystoneScannerOCD {

    @AttributeDefinition(name = "CloudPublisher Target Filter", //
            description = "Specifies, as an OSGi target filter, the pid of the Cloud Publisher used to publish messages to the cloud platform.", //
            required = true //
    )
    String CloudPublisher_target() default "(kura.service.pid=changeme)";

    @AttributeDefinition(name = "Enable Eddystone scanning", //
            description = "Enable scan for Eddystones. Only UID and URL frame types are supported.", //
            required = true //
    )
    boolean enable_scanning() default false;

    @AttributeDefinition(name = "Scan duration", //
            description = "Eddystone scan duration in seconds.", //
            required = true, //
            min = "0" //
    )
    int scan_duration() default 60;

    @AttributeDefinition(name = "Bluetooth interface name", //
            description = "Name of bluetooth adapter.", //
            required = true //
    )
    String iname() default "hci0";

    @AttributeDefinition(name = "Publish period", //
            description = "Shortest time between publishes per beacon in seconds.", //
            required = true, //
            min = "0" //
    )
    int publish_period() default 10;
}
