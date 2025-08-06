/*******************************************************************************
 * Copyright (c) 2017, 2025 Eurotech and/or its affiliates and others
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

import org.eclipse.kura.KuraBluetoothBeaconAdvertiserNotAvailable;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.ble.eddystone.BluetoothLeEddystone;
import org.eclipse.kura.ble.eddystone.BluetoothLeEddystoneService;
import org.eclipse.kura.bluetooth.le.BluetoothLeAdapter;
import org.eclipse.kura.bluetooth.le.BluetoothLeService;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeaconAdvertiser;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        service = { ConfigurableComponent.class }, //
        enabled = true, //
        name = "org.eclipse.kura.example.eddystone.advertiser.EddystoneAdvertiser" //

)
@Designate(ocd = EddystoneAdvertiserOCD.class, factory = false)
public class EddystoneAdvertiser implements ConfigurableComponent {

    private static final Logger logger = LoggerFactory.getLogger(EddystoneAdvertiser.class);

    private BluetoothLeService bluetoothLeService;
    private BluetoothLeAdapter bluetoothLeAdapter;
    private BluetoothLeEddystoneService bluetoothLeEddystoneService;
    private BluetoothLeBeaconAdvertiser<BluetoothLeEddystone> advertising;
    private EddystoneAdvertiserOptions options;

    @Reference(name = "BluetoothLeService", //
            policy = ReferencePolicy.STATIC, //
            cardinality = ReferenceCardinality.MANDATORY, //
            unbind = "unsetBluetoothLeService" //
    )
    public void setBluetoothLeService(BluetoothLeService bluetoothLeService) {
        this.bluetoothLeService = bluetoothLeService;
    }

    public void unsetBluetoothLeService(BluetoothLeService bluetoothLeService) {
        this.bluetoothLeService = null;
    }

    @Reference(name = "BluetoothLeEddystoneService", //
            policy = ReferencePolicy.STATIC, //
            cardinality = ReferenceCardinality.MANDATORY, //
            unbind = "unsetBluetoothLeEddystoneService" //
    )
    public void setBluetoothLeEddystoneService(BluetoothLeEddystoneService bluetoothLeEddystoneService) {
        this.bluetoothLeEddystoneService = bluetoothLeEddystoneService;
    }

    public void unsetBluetoothLeEddystoneService(BluetoothLeEddystoneService bluetoothLeEddystoneService) {
        this.bluetoothLeEddystoneService = null;
    }

    // --------------------------------------------------------------------
    //
    // Activation APIs
    //
    // --------------------------------------------------------------------
    @Activate
    protected void activate(EddystoneAdvertiserOCD ocd) {
        logger.info("Activating Bluetooth Eddystone example...");

        update(ocd);

        logger.debug("Activating Eddystone Example... Done.");

    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        logger.debug("Deactivating Eddystone Example...");

        // Stop the advertising
        if (this.advertising != null) {
            try {
                this.advertising.stopBeaconAdvertising();
                this.bluetoothLeEddystoneService.deleteBeaconAdvertiser(this.advertising);
            } catch (KuraException e) {
                logger.error("Stop Eddystone advertising failed", e);
            }
        }

        // cancel bluetoothAdapter
        this.bluetoothLeAdapter = null;

        logger.debug("Deactivating Eddystone Example... Done.");
    }

    @Modified
    protected void updated(EddystoneAdvertiserOCD ocd) {
        logger.info("Updating Bluetooth Eddystone example...");

        update(ocd);

        logger.debug("Updating Eddystone Example... Done.");
    }

    // --------------------------------------------------------------------
    //
    // Private methods
    //
    // --------------------------------------------------------------------

    private void update(EddystoneAdvertiserOCD ocd) {
        this.options = new EddystoneAdvertiserOptions(ocd);

        // Stop the advertising
        if (this.advertising != null) {
            try {
                this.advertising.stopBeaconAdvertising();
                this.bluetoothLeEddystoneService.deleteBeaconAdvertiser(this.advertising);
            } catch (KuraException e) {
                logger.error("Stop iBeacon advertising failed", e);
            }
        }

        // cancel bluetoothAdapter
        this.bluetoothLeAdapter = null;

        // Get Bluetooth adapter with Beacon capabilities and ensure it is enabled
        if (this.options.isEnabled()) {
            this.bluetoothLeAdapter = this.bluetoothLeService.getAdapter(this.options.getIname());
            if (this.bluetoothLeAdapter != null) {
                logger.info("Bluetooth adapter interface => {}", this.options.getIname());
                logger.info("Bluetooth adapter address => {}", this.bluetoothLeAdapter.getAddress());

                if (!this.bluetoothLeAdapter.isPowered()) {
                    logger.info("Enabling bluetooth adapter...");
                    this.bluetoothLeAdapter.setPowered(true);
                }

                try {
                    this.advertising = this.bluetoothLeEddystoneService.newBeaconAdvertiser(this.bluetoothLeAdapter);
                    configureBeacon();
                } catch (KuraBluetoothBeaconAdvertiserNotAvailable e) {
                    logger.error("Beacon Advertiser not available on {}", this.bluetoothLeAdapter.getInterfaceName(),
                            e);
                }
            }
        } else {
            logger.warn("No Bluetooth adapter found ...");
        }
    }

    private void configureBeacon() {

        try {
            BluetoothLeEddystone eddystone = new BluetoothLeEddystone();
            if ("UID".equals(this.options.getEddystoneFrametype())) {
                eddystone.configureEddystoneUIDFrame(hexToByteArray(this.options.getUidNamespace()),
                        hexToByteArray(this.options.getUidInstance()), this.options.getTxPower().shortValue());
            } else if ("URL".equals(this.options.getEddystoneFrametype())) {
                eddystone.configureEddystoneURLFrame(this.options.getUrlUrl(), this.options.getTxPower().shortValue());
            }
            this.advertising.updateBeaconAdvertisingData(eddystone);
            this.advertising.updateBeaconAdvertisingInterval(this.options.getMinInterval(),
                    this.options.getMaxInterval());

            this.advertising.startBeaconAdvertising();
        } catch (KuraException e) {
            logger.error("IBeacon configuration failed", e);
        }
    }

    private static byte[] hexToByteArray(String hex) {
        byte[] hexArray = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            hexArray[i
                    / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return hexArray;
    }
}
