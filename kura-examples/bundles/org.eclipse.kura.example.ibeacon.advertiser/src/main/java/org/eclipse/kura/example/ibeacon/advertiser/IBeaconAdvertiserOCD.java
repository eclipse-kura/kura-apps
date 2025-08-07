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

package org.eclipse.kura.example.ibeacon.advertiser;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Icon;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition( //
        id = "org.eclipse.kura.example.ibeacon.advertiser.IBeaconAdvertiser", //
        name = "iBeacon Advertiser", //
        description = "The iBeacon Advertiser broadcasts iBeacon frames.", //
        icon = { //
                @Icon(resource = "http://s3.amazonaws.com/kura-resources/application/icon/beacon.png", //
                        size = 32) //
        } //

)
@ComponentPropertyType
public @interface IBeaconAdvertiserOCD {

    @AttributeDefinition(name = "Enable Advertising", //
            description = "Enable Beacon advertising.", //
            required = true //
    )
    boolean enable_advertising() default false;

    @AttributeDefinition(name = "Minimum Beacon advertising interval", //
            description = "Minimum time interval between beacons (milliseconds).", //
            required = true, //
            min = "0" //
    )
    int min_beacon_interval() default 1000;

    @AttributeDefinition(name = "Maximum Beacon advertising interval", //
            description = "Maximum time interval between beacons (milliseconds).", //
            required = true, //
            min = "0" //
    )
    int max_beacon_interval() default 1000;

    @AttributeDefinition(name = "UUID", //
            description = "128-bit uuid for beacon advertising expressed as hex string.", //
            required = true //
    )
    String uuid() default "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

    @AttributeDefinition(name = "major", //
            description = "Major value.", //
            required = true, //
            min = "0", //
            max = "65535" //
    )
    int major() default 0;

    @AttributeDefinition(name = "minor", //
            description = "Minor value.", //
            required = true, //
            min = "0", //
            max = "65535" //
    )
    int minor() default 0;

    @AttributeDefinition(name = "Transmission power", //
            description = "Transmission power measured at 1m away from the beacon expressed in dBm. Transmission power range: min. -127, max. 126.", //
            required = true, //
            min = "-127", //
            max = "126" //
    )
    int tx_power() default 0;

    @AttributeDefinition(name = "Bluetooth interface name", //
            description = "Name of bluetooth adapter.", //
            required = true //
    )
    String iname() default "hci0";
}
