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

import org.eclipse.kura.KuraBluetoothBeaconAdvertiserNotAvailable;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.ble.ibeacon.BluetoothLeIBeacon;
import org.eclipse.kura.ble.ibeacon.BluetoothLeIBeaconService;
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
        name = "org.eclipse.kura.example.ibeacon.advertiser.IBeaconAdvertiser" //

)
@Designate(ocd = IBeaconAdvertiserOCD.class, factory = false)
public class IBeaconAdvertiser implements ConfigurableComponent {

    private static final Logger logger = LoggerFactory.getLogger(IBeaconAdvertiser.class);

    private BluetoothLeService bluetoothLeService;
    private BluetoothLeAdapter bluetoothLeAdapter;
    private BluetoothLeIBeaconService bluetoothLeIBeaconService;
    private BluetoothLeBeaconAdvertiser<BluetoothLeIBeacon> advertising;
    private IBeaconAdvertiserOptions options;

    @Reference(name = "BluetoothLeService", //
            policy = ReferencePolicy.STATIC, //
            cardinality = ReferenceCardinality.MANDATORY, //
            unbind = "unsetBluetoothLeService" //
    )
    public void setBluetoothLeService(final BluetoothLeService bluetoothLeService) {
        this.bluetoothLeService = bluetoothLeService;
    }

    public void unsetBluetoothLeService(final BluetoothLeService bluetoothLeService) {
        this.bluetoothLeService = null;
    }

    @Reference(name = "BluetoothLeIBeaconService", //
            policy = ReferencePolicy.STATIC, //
            cardinality = ReferenceCardinality.MANDATORY, //
            unbind = "unsetBluetoothLeIBeaconService" //
    )
    public void setBluetoothLeIBeaconService(final BluetoothLeIBeaconService bluetoothLeIBeaconService) {
        this.bluetoothLeIBeaconService = bluetoothLeIBeaconService;
    }

    public void unsetBluetoothLeIBeaconService(final BluetoothLeIBeaconService bluetoothLeIBeaconService) {
        this.bluetoothLeIBeaconService = null;
    }

    // --------------------------------------------------------------------
    //
    // Activation APIs
    //
    // --------------------------------------------------------------------
    @Activate
    protected void activate(final ComponentContext context, final IBeaconAdvertiserOCD ocd) {
        IBeaconAdvertiser.logger.info("Activating Bluetooth iBeacon example...");

        executeUpdate(ocd);

        IBeaconAdvertiser.logger.debug("Activating iBeacon Example... Done.");

    }

    @Deactivate
    protected void deactivate(final IBeaconAdvertiserOCD ocd) {

        IBeaconAdvertiser.logger.debug("Deactivating iBeacon Example...");

        // Stop the advertising
        if (this.advertising != null) {
            try {
                this.advertising.stopBeaconAdvertising();
                this.bluetoothLeIBeaconService.deleteBeaconAdvertiser(this.advertising);
            } catch (final KuraException e) {
                IBeaconAdvertiser.logger.error("Stop iBeacon advertising failed", e);
            }
        }

        // cancel bluetoothAdapter
        this.bluetoothLeAdapter = null;

        IBeaconAdvertiser.logger.debug("Deactivating iBeacon Example... Done.");
    }

    @Modified
    protected void updated(final IBeaconAdvertiserOCD ocd) {
        IBeaconAdvertiser.logger.info("Updating Bluetooth iBeacon example...");

        executeUpdate(ocd);

        IBeaconAdvertiser.logger.debug("Updating iBeacon Example... Done.");
    }

    // --------------------------------------------------------------------
    //
    // Private methods
    //
    // --------------------------------------------------------------------

    private void executeUpdate(final IBeaconAdvertiserOCD ocd) {
        this.options = new IBeaconAdvertiserOptions(ocd);

        // Stop the advertising
        if (this.advertising != null) {
            try {
                this.advertising.stopBeaconAdvertising();
                this.bluetoothLeIBeaconService.deleteBeaconAdvertiser(this.advertising);
            } catch (final KuraException e) {
                IBeaconAdvertiser.logger.error("Stop iBeacon advertising failed", e);
            }
        }

        // cancel bluetoothAdapter
        this.bluetoothLeAdapter = null;

        // Get Bluetooth adapter with Beacon capabilities and ensure it is enabled
        if (this.options.isEnabled()) {
            this.bluetoothLeAdapter = this.bluetoothLeService.getAdapter(this.options.getIname());
            if (this.bluetoothLeAdapter != null) {
                IBeaconAdvertiser.logger.info("Bluetooth adapter interface => {}", this.options.getIname());
                IBeaconAdvertiser.logger.info("Bluetooth adapter address => {}", this.bluetoothLeAdapter.getAddress());

                if (!this.bluetoothLeAdapter.isPowered()) {
                    IBeaconAdvertiser.logger.info("Enabling bluetooth adapter...");
                    this.bluetoothLeAdapter.setPowered(true);
                }

                try {
                    this.advertising = this.bluetoothLeIBeaconService.newBeaconAdvertiser(this.bluetoothLeAdapter);
                    configureBeacon();
                } catch (final KuraBluetoothBeaconAdvertiserNotAvailable e) {
                    IBeaconAdvertiser.logger.error("Beacon Advertiser not available on {}",
                            this.bluetoothLeAdapter.getInterfaceName(), e);
                }

            } else {
                IBeaconAdvertiser.logger.warn("No Bluetooth adapter found ...");
            }
        }
    }

    private void configureBeacon() {
        try {
            final BluetoothLeIBeacon iBeacon = new BluetoothLeIBeacon(this.options.getUUID(),
                    this.options.getMajor().shortValue(), this.options.getMinor().shortValue(),
                    this.options.getTxPower().shortValue());
            this.advertising.updateBeaconAdvertisingData(iBeacon);
            this.advertising.updateBeaconAdvertisingInterval(this.options.getMinInterval(),
                    this.options.getMaxInterval());

            this.advertising.startBeaconAdvertising();
        } catch (final KuraException e) {
            IBeaconAdvertiser.logger.error("IBeacon configuration failed", e);
        }
    }

}
