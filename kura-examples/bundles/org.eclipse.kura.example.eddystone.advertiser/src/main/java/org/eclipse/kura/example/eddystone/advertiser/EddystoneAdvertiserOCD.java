package org.eclipse.kura.example.eddystone.advertiser;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Icon;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition( //
        id = "org.eclipse.kura.example.eddystone.advertiser.EddystoneAdvertiser", //
        name = "Eddystone Advertiser", //
        description = "The Eddystone Advertiser broadcasts UID or URL frame types.", //
        icon = { //
                @Icon(resource = "http://s3.amazonaws.com/kura-resources/application/icon/eddystone_logo.png", //
                        size = 32) //
        } //

)
@ComponentPropertyType
public @interface EddystoneAdvertiserOCD {

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
    int minimum_beacon_advertising_interval() default 1000;

    @AttributeDefinition(name = "Maximum Beacon advertising interval", //
            description = "Maximum time interval between beacons (milliseconds).", //
            required = true, //
            min = "0" //
    )
    int maximum_beacon_advertising_interval() default 1000;

    @AttributeDefinition(name = "Transmission power", //
            description = "Transmission power measured at 1m away from the beacon expressed in dBm. " //
                    + "Transmission power range: min. -127, max. 126.", //
            required = true, //
            min = "-127", //
            max = "126" //
    )
    int transmission_power() default 0;

    @AttributeDefinition(//
            name = "Eddystone Frame Type", //
            description = "Eddystone frame type", //
            required = true, //
            options = { //
                    @Option(label = "UID", value = "UID"), //
                    @Option(label = "URL", value = "URL") //
            })
    String eddystone_frame_type() default "UID";

    @AttributeDefinition(name = "Namespace for Eddystone UID frame", //
            description = "The 10-byte namespace for Eddystone UID frame in hexadecimal.", //
            required = true //
    )
    String eddystone_uid_namespace() default "00112233445566778899";

    @AttributeDefinition(name = "Instance for Eddystone UID frame", //
            description = "The 6-byte instance for Eddystone UID frame in hexadecimal.", //
            required = true //
    )
    String eddystone_uid_instance() default "001122334455";

    @AttributeDefinition(name = "Url for Eddystone URL frame", //
            description = "The url for Eddystone URL frame.", //
            required = true //
    )
    String eddystone_url() default "http://www.eclipse.org/kura";

    @AttributeDefinition(name = "Bluetooth interface name", //
            description = "Name of bluetooth adapter.", //
            required = true //
    )
    String bluetooth_interface_name() default "hci0";
}