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
package org.eclipse.kura.example.ibeacon.advertiser;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IBeaconAdvertiserOptions {

    private static final String PROPERTY_ENABLE = "enable.advertising";
    private static final String PROPERTY_MIN_INTERVAL = "min.beacon.interval";
    private static final String PROPERTY_MAX_INTERVAL = "max.beacon.interval";
    private static final String PROPERTY_UUID = "uuid";
    private static final String PROPERTY_MAJOR = "major";
    private static final String PROPERTY_MINOR = "minor";
    private static final String PROPERTY_TX_POWER = "tx.power";
    private static final String PROPERTY_INAME = "iname";

    private static final boolean PROPERTY_ENABLE_DEFAULT = false;
    private static final int PROPERTY_MIN_INTERVAL_DEFAULT = 1000;
    private static final int PROPERTY_MAX_INTERVAL_DEFAULT = 1000;
    private static final String PROPERTY_UUID_DEFAULT = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
    private static final int PROPERTY_MAJOR_DEFAULT = 0;
    private static final int PROPERTY_MINOR_DEFAULT = 0;
    private static final int PROPERTY_TX_POWER_DEFAULT = 0;
    private static final String PROPERTY_INAME_DEFAULT = "hci0";

    private static final int PROPERTY_MAJOR_MAX = 65535;
    private static final int PROPERTY_MAJOR_MIN = 0;
    private static final int PROPERTY_MINOR_MAX = 65535;
    private static final int PROPERTY_MINOR_MIN = 0;
    private static final short PROPERTY_TX_POWER_MAX = 126;
    private static final short PROPERTY_TX_POWER_MIN = -127;

    private final boolean enable;
    private final Integer minInterval;
    private final Integer maxInterval;
    private UUID uuid;
    private final int major;
    private final int minor;
    private final Integer txPower;
    private final String iname;

    private static final Logger logger = LoggerFactory.getLogger(IBeaconAdvertiserOptions.class);

    public IBeaconAdvertiserOptions(final IBeaconAdvertiserOCD ocd) {
        this.enable = ocd.enable_advertising();
        this.minInterval = (int) (ocd.min_beacon_interval() / 0.625);
        this.maxInterval = (int) (ocd.max_beacon_interval() / 0.625);
        this.major = setInRange(ocd.major(), IBeaconAdvertiserOptions.PROPERTY_MAJOR_MAX,
                IBeaconAdvertiserOptions.PROPERTY_MAJOR_MIN);
        this.minor = setInRange(ocd.minor(), IBeaconAdvertiserOptions.PROPERTY_MINOR_MAX,
                IBeaconAdvertiserOptions.PROPERTY_MINOR_MIN);
        this.txPower = setInRange(ocd.tx_power(), IBeaconAdvertiserOptions.PROPERTY_TX_POWER_MAX,
                IBeaconAdvertiserOptions.PROPERTY_TX_POWER_MIN);
        this.iname = ocd.iname();
        final String uuidString = ocd.uuid();
        if (uuidString.trim().replace("-", "").length() != 32) {
            IBeaconAdvertiserOptions.logger.warn("UUID is too short or too long!");
            this.uuid = UUID.fromString(IBeaconAdvertiserOptions.PROPERTY_UUID_DEFAULT);
        } else {
            this.uuid = UUID.fromString(setInHex(uuidString, IBeaconAdvertiserOptions.PROPERTY_UUID_DEFAULT));
        }
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

    public UUID getUUID() {
        return this.uuid;
    }

    public Integer getMajor() {
        return this.major;
    }

    public Integer getMinor() {
        return this.minor;
    }

    public Integer getTxPower() {
        return this.txPower;
    }

    public String getIname() {
        return this.iname;
    }

    private int setInRange(final int value, final int max, final int min) {
        if (value <= max && value >= min) {
            return value;
        } else {
            return (value > max) ? max : min;
        }
    }

    private String setInHex(final String value, final String defaultValue) {
        if (!value.trim().replace("-", "").matches("^[0-9a-fA-F]+$")) {
            return defaultValue;
        } else {
            return value;
        }
    }
}
