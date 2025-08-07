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
package org.eclipse.kura.example.ibeacon.scanner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.ble.ibeacon.BluetoothLeIBeacon;
import org.eclipse.kura.ble.ibeacon.BluetoothLeIBeaconService;
import org.eclipse.kura.bluetooth.le.BluetoothLeAdapter;
import org.eclipse.kura.bluetooth.le.BluetoothLeService;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeaconScanner;
import org.eclipse.kura.bluetooth.le.beacon.listener.BluetoothLeBeaconListener;
import org.eclipse.kura.cloudconnection.message.KuraMessage;
import org.eclipse.kura.cloudconnection.publisher.CloudPublisher;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.message.KuraPayload;
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
        enabled = true, //
        name = "org.eclipse.kura.example.ibeacon.scanner.IBeaconScanner", //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        service = { ConfigurableComponent.class } //
)
@Designate(ocd = IBeaconScannerOCD.class, factory = false)
public class IBeaconScanner implements ConfigurableComponent, BluetoothLeBeaconListener<BluetoothLeIBeacon> {

    private static final String ADDRESS_MESSAGE_PROP_KEY = "address";

    private static final Logger logger = LoggerFactory.getLogger(IBeaconScanner.class);

    private ExecutorService worker;
    private Future<?> handle;

    private BluetoothLeService bluetoothLeService;
    private BluetoothLeIBeaconService bluetoothLeIBeaconService;
    private BluetoothLeBeaconScanner<BluetoothLeIBeacon> bluetoothLeIBeaconScanner;
    private Map<String, Long> publishTimes;
    private IBeaconScannerOptions options;

    private CloudPublisher cloudPublisher;

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

    @Reference(name = "CloudPublisher", //
            policy = ReferencePolicy.DYNAMIC, //
            cardinality = ReferenceCardinality.OPTIONAL, //
            unbind = "unsetCloudPublisher" //
    )
    public void setCloudPublisher(final CloudPublisher cloudPublisher) {
        this.cloudPublisher = cloudPublisher;
    }

    public void unsetCloudPublisher(final CloudPublisher cloudPublisher) {
        this.cloudPublisher = null;
    }

    @Activate
    protected void activate(final ComponentContext context, final IBeaconScannerOCD ocd) {
        IBeaconScanner.logger.info("Activating Bluetooth iBeacon Scanner example...");

        this.publishTimes = new HashMap<>();
        doUpdate(ocd);
        IBeaconScanner.logger.info("Activating Bluetooth iBeacon Scanner example...Done");
    }

    @Deactivate
    protected void deactivate(final ComponentContext context) {
        IBeaconScanner.logger.debug("Deactivating iBeacon Scanner Example...");

        releaseResources();

        if (this.handle != null) {
            this.handle.cancel(true);
        }

        if (this.worker != null) {
            this.worker.shutdown();
        }

        IBeaconScanner.logger.debug("Deactivating iBeacon Scanner Example... Done.");
    }

    @Modified
    protected void updated(final IBeaconScannerOCD ocd) {
        IBeaconScanner.logger.debug("Updating iBeacon Scanner Example...");

        releaseResources();

        if (this.handle != null) {
            this.handle.cancel(true);
        }

        if (this.worker != null) {
            this.worker.shutdown();
        }

        doUpdate(ocd);

        IBeaconScanner.logger.debug("Updating iBeacon Scanner Example... Done");
    }

    private void doUpdate(final IBeaconScannerOCD ocd) {
        this.options = new IBeaconScannerOptions(ocd);

        if (this.options.isEnabled()) {
            this.worker = Executors.newSingleThreadExecutor();
            this.handle = this.worker.submit(this::setup);
        }
    }

    private void setup() {
        final BluetoothLeAdapter bluetoothLeAdapter = this.bluetoothLeService.getAdapter(this.options.getAdapterName());
        if (bluetoothLeAdapter != null) {
            if (!bluetoothLeAdapter.isPowered()) {
                bluetoothLeAdapter.setPowered(true);
            }
            this.bluetoothLeIBeaconScanner = this.bluetoothLeIBeaconService.newBeaconScanner(bluetoothLeAdapter);
            this.bluetoothLeIBeaconScanner.addBeaconListener(this);
            try {
                this.bluetoothLeIBeaconScanner.startBeaconScan(this.options.getScanDuration());
            } catch (final KuraException e) {
                IBeaconScanner.logger.error("iBeacon scanning failed", e);
            }
        } else {
            IBeaconScanner.logger.warn("No Bluetooth adapter found ...");
        }
    }

    private void releaseResources() {
        if (this.bluetoothLeIBeaconScanner != null) {
            if (this.bluetoothLeIBeaconScanner.isScanning()) {
                this.bluetoothLeIBeaconScanner.stopBeaconScan();
            }
            this.bluetoothLeIBeaconScanner.removeBeaconListener(this);
            this.bluetoothLeIBeaconService.deleteBeaconScanner(this.bluetoothLeIBeaconScanner);
        }
    }

    private double calculateDistance(final int rssi, final int txpower) {

        final int ratioDB = txpower - rssi;
        final double ratioLinear = Math.pow(10, (double) ratioDB / 10);
        return Math.sqrt(ratioLinear);
    }

    @Override
    public void onBeaconsReceived(final BluetoothLeIBeacon iBeacon) {
        IBeaconScanner.logger.info("iBeacon received from {}", iBeacon.getAddress());
        IBeaconScanner.logger.info("UUID : {}", iBeacon.getUuid());
        IBeaconScanner.logger.info("Major : {}", iBeacon.getMajor());
        IBeaconScanner.logger.info("Minor : {}", iBeacon.getMinor());
        IBeaconScanner.logger.info("TxPower : {}", iBeacon.getTxPower());
        IBeaconScanner.logger.info("RSSI : {}", iBeacon.getRssi());
        final long now = System.currentTimeMillis();

        final Long lastPublishTime = this.publishTimes.get(iBeacon.getAddress());

        // If this beacon is new, or it last published more than 'rateLimit' seconds ago
        if (lastPublishTime == null || now - lastPublishTime > this.options.getPublishPeriod() * 1000L) {

            // Store the publish time against the address
            this.publishTimes.put(iBeacon.getAddress(), now);

            if (this.cloudPublisher == null) {
                IBeaconScanner.logger.info("No cloud publisher selected. Cannot publish!");
                return;
            }

            // Publish the beacon data to the beacon's topic
            final KuraPayload kp = new KuraPayload();
            kp.setTimestamp(new Date());
            kp.addMetric("uuid", iBeacon.getUuid().toString());
            kp.addMetric("txpower", (int) iBeacon.getTxPower());
            kp.addMetric("rssi", iBeacon.getRssi());
            kp.addMetric("major", (int) iBeacon.getMajor());
            kp.addMetric("minor", (int) iBeacon.getMinor());
            kp.addMetric("distance", calculateDistance(iBeacon.getRssi(), iBeacon.getTxPower()));

            final Map<String, Object> properties = new HashMap<>();
            properties.put(IBeaconScanner.ADDRESS_MESSAGE_PROP_KEY, iBeacon.getAddress());

            final KuraMessage message = new KuraMessage(kp, properties);
            try {
                this.cloudPublisher.publish(message);
            } catch (final KuraException e) {
                IBeaconScanner.logger.error("Unable to publish", e);
            }
        }
    }
}
