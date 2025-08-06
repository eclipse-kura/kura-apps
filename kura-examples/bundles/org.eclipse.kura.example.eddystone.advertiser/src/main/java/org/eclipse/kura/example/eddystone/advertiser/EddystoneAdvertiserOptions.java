/*******************************************************************************
 * Copyright (c) 2017, 2020 Eurotech and/or its affiliates and others
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
package org.eclipse.kura.example.eddystone.advertiser;

public class EddystoneAdvertiserOptions {

    private static final String PROPERTY_NAMESPACE_DEFAULT = "00112233445566778899";
    private static final String PROPERTY_INSTANCE_DEFAULT = "001122334455";

    private static final int PROPERTY_TX_POWER_MAX = 126;
    private static final int PROPERTY_TX_POWER_MIN = -127;

    private final boolean enable;
    private final Integer minInterval;
    private final Integer maxInterval;
    private final String eddystoneFrametype;
    private final String uidNamespace;
    private final String uidInstance;
    private final String urlUrl;
    private final Integer txPower;
    private final String iname;

    public EddystoneAdvertiserOptions(EddystoneAdvertiserOCD ocd) {
        this.enable = ocd.enable_advertising();
        this.minInterval = ocd.minimum_beacon_advertising_interval();
        this.maxInterval = ocd.maximum_beacon_advertising_interval();
        this.eddystoneFrametype = ocd.eddystone_frame_type();
        this.urlUrl = ocd.eddystone_url();

        int txPowerInt = ocd.transmission_power();
        if (txPowerInt <= PROPERTY_TX_POWER_MAX && txPowerInt >= PROPERTY_TX_POWER_MIN) {
            this.txPower = txPowerInt;
        } else if (txPowerInt > PROPERTY_TX_POWER_MAX) {
            this.txPower = PROPERTY_TX_POWER_MAX;
        } else {
            this.txPower = PROPERTY_TX_POWER_MIN;
        }

        this.iname = ocd.bluetooth_interface_name();
        this.uidNamespace = setInPropertyLimit(ocd.eddystone_uid_namespace(), PROPERTY_NAMESPACE_DEFAULT, 20);
        this.uidInstance = setInPropertyLimit(ocd.eddystone_uid_instance(), PROPERTY_INSTANCE_DEFAULT, 12);
    }

    public boolean isEnabled() {
        return this.enable;
    }

    public Integer getMinInterval() {
        return this.minInterval;
    }

    public Integer getMaxInterval() {
        return this.maxInterval;
    }

    public String getEddystoneFrametype() {
        return this.eddystoneFrametype;
    }

    public String getUidNamespace() {
        return this.uidNamespace;
    }

    public String getUidInstance() {
        return this.uidInstance;
    }

    public String getUrlUrl() {
        return this.urlUrl;
    }

    public Integer getTxPower() {
        return this.txPower;
    }

    public String getIname() {
        return this.iname;
    }

    private String setInPropertyLimit(String property, String propertyDefault, int lengthLimit) {
        if (property.length() == lengthLimit) {
            return setInHex(property, propertyDefault);
        } else {
            return propertyDefault;
        }
    }

    private String setInHex(String value, String defaultValue) {
        if (!value.matches("^[0-9a-fA-F]+$")) {
            return defaultValue;
        } else {
            return value;
        }
    }
}
