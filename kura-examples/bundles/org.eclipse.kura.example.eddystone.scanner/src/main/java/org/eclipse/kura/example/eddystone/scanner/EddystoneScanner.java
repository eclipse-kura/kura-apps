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
package org.eclipse.kura.example.eddystone.scanner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.ble.eddystone.BluetoothLeEddystone;
import org.eclipse.kura.ble.eddystone.BluetoothLeEddystoneService;
import org.eclipse.kura.bluetooth.le.BluetoothLeAdapter;
import org.eclipse.kura.bluetooth.le.BluetoothLeService;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeaconScanner;
import org.eclipse.kura.bluetooth.le.beacon.listener.BluetoothLeBeaconListener;
import org.eclipse.kura.cloudconnection.message.KuraMessage;
import org.eclipse.kura.cloudconnection.publisher.CloudPublisher;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.message.KuraPayload;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, //
        enabled = true, //
        name = "org.eclipse.kura.example.eddystone.scanner.EddystoneScanner", //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        service = { ConfigurableComponent.class } //
)
@Designate(ocd = EddystoneScannerOCD.class, factory = false)
public class EddystoneScanner implements ConfigurableComponent, BluetoothLeBeaconListener<BluetoothLeEddystone> {

    private static final String ADDRESS_MESSAGE_PROP_KEY = "address";

    private static final Logger logger = LoggerFactory.getLogger(EddystoneScanner.class);

    private ExecutorService worker;
    private Future<?> handle;

    private BluetoothLeService bluetoothLeService;
    private BluetoothLeEddystoneService bluetoothLeEddystoneService;
    private BluetoothLeBeaconScanner<BluetoothLeEddystone> bluetoothLeEddystoneScanner;
    private Map<String, Long> publishTimes;
    private EddystoneScannerOptions options;

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

    @Reference(name = "BluetoothLeEddystoneService", //
            policy = ReferencePolicy.STATIC, //
            cardinality = ReferenceCardinality.MANDATORY, //
            unbind = "unsetBluetoothLeEddystoneService" //
    )
    public void setBluetoothLeEddystoneService(final BluetoothLeEddystoneService bluetoothLeEddystoneService) {
        this.bluetoothLeEddystoneService = bluetoothLeEddystoneService;
    }

    public void unsetBluetoothLeEddystoneService(final BluetoothLeEddystoneService bluetoothLeEddystoneService) {
        this.bluetoothLeEddystoneService = null;
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

    protected void activate(final EddystoneScannerOCD ocd) {
        EddystoneScanner.logger.info("Activating Bluetooth Eddystone Scanner example...");

        this.publishTimes = new HashMap<>();
        doUpdate(ocd);
        EddystoneScanner.logger.info("Activating Bluetooth Eddystone Scanner example...Done");
    }

    protected void deactivate(final ComponentContext context) {
        EddystoneScanner.logger.debug("Deactivating Eddystone Scanner Example...");

        releaseResources();

        if (this.handle != null) {
            this.handle.cancel(true);
        }

        if (this.worker != null) {
            this.worker.shutdown();
        }

        EddystoneScanner.logger.debug("Deactivating Eddystone Scanner Example... Done.");
    }

    protected void updated(final EddystoneScannerOCD ocd) {
        EddystoneScanner.logger.debug("Updating Eddystone Scanner Example...");

        releaseResources();

        if (this.handle != null) {
            this.handle.cancel(true);
        }

        if (this.worker != null) {
            this.worker.shutdown();
        }

        doUpdate(ocd);

        EddystoneScanner.logger.debug("Updating Eddystone Scanner Example... Done");
    }

    private void doUpdate(final EddystoneScannerOCD ocd) {
        this.options = new EddystoneScannerOptions(ocd);

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
            this.bluetoothLeEddystoneScanner = this.bluetoothLeEddystoneService.newBeaconScanner(bluetoothLeAdapter);
            this.bluetoothLeEddystoneScanner.addBeaconListener(this);
            try {
                this.bluetoothLeEddystoneScanner.startBeaconScan(this.options.getScanDuration());
            } catch (final KuraException e) {
                EddystoneScanner.logger.error("iBeacon scanning failed", e);
            }
        } else {
            EddystoneScanner.logger.warn("No Bluetooth adapter found ...");
        }
    }

    private void releaseResources() {
        if (this.bluetoothLeEddystoneScanner != null) {
            if (this.bluetoothLeEddystoneScanner.isScanning()) {
                this.bluetoothLeEddystoneScanner.stopBeaconScan();
            }
            this.bluetoothLeEddystoneScanner.removeBeaconListener(this);
            this.bluetoothLeEddystoneService.deleteBeaconScanner(this.bluetoothLeEddystoneScanner);
        }
    }

    private double calculateDistance(final int rssi, final int txpower) {

        final int ratioDB = txpower - rssi;
        final double ratioLinear = Math.pow(10, (double) ratioDB / 10);
        return Math.sqrt(ratioLinear);
    }

    @Override
    public void onBeaconsReceived(final BluetoothLeEddystone eddystone) {
        EddystoneScanner.logger.info("Eddystone {} received from {}", eddystone.getFrameType(), eddystone.getAddress());
        if ("UID".equals(eddystone.getFrameType())) {
            EddystoneScanner.logger.info("Namespace : {}",
                    EddystoneScanner.bytesArrayToHexString(eddystone.getNamespace()));
            EddystoneScanner.logger.info("Instance : {}",
                    EddystoneScanner.bytesArrayToHexString(eddystone.getInstance()));
        } else if ("URL".equals(eddystone.getFrameType())) {
            EddystoneScanner.logger.info("URL : {}", eddystone.getUrlScheme() + eddystone.getUrl());
        }
        EddystoneScanner.logger.info("TxPower : {}", eddystone.getTxPower());
        EddystoneScanner.logger.info("RSSI : {}", eddystone.getRssi());
        final long now = System.currentTimeMillis();

        final Long lastPublishTime = this.publishTimes.get(eddystone.getAddress());

        // If this beacon is new, or it last published more than 'publish.period'
        // seconds ago
        if (lastPublishTime == null || now - lastPublishTime > this.options.getPublishPeriod() * 1000L) {

            // Store the publish time against the address
            this.publishTimes.put(eddystone.getAddress(), now);

            if (this.cloudPublisher == null) {
                EddystoneScanner.logger.info("No cloud publisher selected. Cannot publish!");
                return;
            }

            // Publish the beacon data to the beacon's topic
            final KuraPayload kp = new KuraPayload();
            kp.setTimestamp(new Date());
            kp.addMetric("type", eddystone.getFrameType());
            if ("UID".equals(eddystone.getFrameType())) {
                kp.addMetric("namespace", EddystoneScanner.bytesArrayToHexString(eddystone.getNamespace()));
                kp.addMetric("instance", EddystoneScanner.bytesArrayToHexString(eddystone.getInstance()));
            } else if ("URL".equals(eddystone.getFrameType())) {
                kp.addMetric("URL", eddystone.getUrl());
            }
            kp.addMetric("txpower", (int) eddystone.getTxPower());
            kp.addMetric("rssi", eddystone.getRssi());
            kp.addMetric("distance", calculateDistance(eddystone.getRssi(), eddystone.getTxPower()));

            final Map<String, Object> properties = new HashMap<>();
            properties.put(EddystoneScanner.ADDRESS_MESSAGE_PROP_KEY, eddystone.getAddress());

            final KuraMessage message = new KuraMessage(kp, properties);

            try {
                this.cloudPublisher.publish(message);
            } catch (final KuraException e) {
                EddystoneScanner.logger.error("Unable to publish", e);
            }
        }
    }

    private static String bytesArrayToHexString(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        for (final byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
