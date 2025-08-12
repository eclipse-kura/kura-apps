/*******************************************************************************
 * Copyright (c) 2011, 2025 Eurotech and/or its affiliates and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *  Eurotech
 *  Red Hat Inc
 *******************************************************************************/
package org.eclipse.kura.demo.modbus;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.kura.cloudconnection.listener.CloudConnectionListener;
import org.eclipse.kura.cloudconnection.listener.CloudDeliveryListener;
import org.eclipse.kura.cloudconnection.message.KuraMessage;
import org.eclipse.kura.cloudconnection.publisher.CloudPublisher;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.message.KuraPayload;
import org.eclipse.kura.protocol.modbus.ModbusProtocolDeviceService;
import org.eclipse.kura.protocol.modbus.ModbusProtocolException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        service = { ConfigurableComponent.class, CloudConnectionListener.class, CloudDeliveryListener.class }, //
        name = "org.eclipse.kura.demo.modbus.ModbusExample", //
        enabled = true //
)
@Designate(ocd = ModbusExampleOCD.class, factory = false)
public class ModbusExample implements ConfigurableComponent, CloudConnectionListener, CloudDeliveryListener {

    private static final Logger logger = LoggerFactory.getLogger(ModbusExample.class);

    // Publishing Property Names
    private static final String MODBUS_PROTOCOL = "modbus.protocol";
    private static final String MODBUS_SLAVE_ADDRESS = "slave.address";

    private static final String SERIAL_DEVICE_PROP_NAME = "serial.port";
    private static final String SERIAL_BAUDRATE_PROP_NAME = "serial.baudrate";
    private static final String SERIAL_DATA_BITS_PROP_NAME = "serial.data.bits";
    private static final String SERIAL_PARITY_PROP_NAME = "serial.parity";
    private static final String SERIAL_STOP_BITS_PROP_NAME = "serial.stop.bits";

    private static final String ETHERNET_IP_ADDRESS = "ip.address";
    private static final String ETHERNET_TCP_PORT = "tcp.port";

    private static final String POLL_INTERVAL = "poll.interval";
    private static final String PUBLISH_INTERVAL = "publish.interval";

    private static final String INPUT_ADDRESS = "input.address";
    private static final String REGISTER_ADDRESS = "register.address";

    private boolean doConnection = true;

    private ScheduledExecutorService worker;
    private Future<?> handle;

    private Properties modbusProperties;
    private boolean configured;
    private int slaveAddr;
    private int publishInterval = 0;
    private long startPublish = 0;

    private int inputaddr = 0;
    private int registeraddr = 0;

    private CloudPublisher cloudPublisher;
    private ModbusProtocolDeviceService protocolDevice;

    @Reference(name = "ModbusProtocolDeviceService", //
            policy = org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC, //
            cardinality = org.osgi.service.component.annotations.ReferenceCardinality.MANDATORY)
    public void setModbusProtocolDeviceService(ModbusProtocolDeviceService modbusService) {
        this.protocolDevice = modbusService;
    }

    public void unsetModbusProtocolDeviceService(ModbusProtocolDeviceService modbusService) {
        this.protocolDevice = null;
    }

    @Reference(name = "CloudPublisher", //
            policy = ReferencePolicy.DYNAMIC, //
            cardinality = ReferenceCardinality.OPTIONAL //
    )
    public void setCloudPublisher(CloudPublisher cloudPublisher) {
        this.cloudPublisher = cloudPublisher;
        this.cloudPublisher.registerCloudConnectionListener(ModbusExample.this);
        this.cloudPublisher.registerCloudDeliveryListener(ModbusExample.this);
    }

    public void unsetCloudPublisher(CloudPublisher cloudPublisher) {
        this.cloudPublisher.unregisterCloudConnectionListener(ModbusExample.this);
        this.cloudPublisher.unregisterCloudDeliveryListener(ModbusExample.this);
        this.cloudPublisher = null;
    }

    // ----------------------------------------------------------------
    //
    // Activation APIs
    //
    // ----------------------------------------------------------------

    protected void activate(ModbusExampleOCD ocd) {
        logger.info("Activating ModbusExample...");

        this.worker = Executors.newSingleThreadScheduledExecutor();

        this.configured = false;
        doUpdate(ocd);
    }

    protected void deactivate() {
        logger.info("ModbusExample deactivate...");
        if (this.handle != null) {
            this.handle.cancel(true);
        }
        logger.info("Modbus polling thread killed");

        if (this.protocolDevice != null) {
            try {
                this.protocolDevice.disconnect();
            } catch (ModbusProtocolException e) {
                logger.error("Failed to disconnect : {}", e.getMessage());
            }
        }
        this.configured = false;
    }

    public void updated(ModbusExampleOCD ocd) {
        logger.info("updated...");
        this.configured = false;
        doUpdate(ocd);
    }

    // ----------------------------------------------------------------
    //
    // Private Methods
    //
    // ----------------------------------------------------------------

    /**
     * Called after a new set of properties has been configured on the service
     */
    private void doUpdate(ModbusExampleOCD ocd) {
        try {

            // cancel a current worker handle if one if active
            if (this.handle != null) {
                this.handle.cancel(true);
            }

            if (this.protocolDevice != null) {
                try {
                    this.protocolDevice.disconnect();
                } catch (ModbusProtocolException e) {
                    logger.error("Failed to disconnect : {}", e.getMessage());
                }
            }
            this.configured = false;

            this.modbusProperties = getModbusProperties(ocd);
            if (this.modbusProperties == null) {
                logger.error("Something is wrong in the properties, program cannot continue");
                return;
            }

            this.publishInterval = ocd.publish_interval();

            this.inputaddr = ocd.input_address();

            this.registeraddr = ocd.register_address();

            if (!this.configured) {
                try {
                    if (this.modbusProperties != null) {
                        configureDevice();
                    }
                } catch (ModbusProtocolException e) {
                    logger.error("ModbusProtocolException :  {}", e.getMessage());
                }
            }

            // schedule a new worker based on the properties of the service

            int pubrate = ocd.poll_interval();
            logger.info("scheduleAtFixedRate {}", pubrate);
            this.handle = this.worker.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    doModbusLoop();
                }
            }, 0, pubrate, TimeUnit.MILLISECONDS);

        } catch (Throwable t) {
            logger.error("Unexpected Throwable", t);
        }
    }

    private boolean doConnectionWork() {
        if (this.cloudPublisher == null) {
            logger.debug("No cloud publisher selected. Cannot publish!");
            return true;
        }

        logger.info("Successfully selected a cloud publisher");
        return false;
    }

    private void doModbusLoop() {

        // Connect the Cloud Client
        if (this.doConnection) {
            this.doConnection = doConnectionWork();
        }

        try {
            // Allocate a new payload
            KuraPayload payload = new KuraPayload();

            if (this.inputaddr > 0) {
                boolean[] dicreteInputs = this.protocolDevice.readDiscreteInputs(this.slaveAddr, this.inputaddr, 1);
                StringBuilder sb = new StringBuilder().append("Input ").append(this.inputaddr).append(" = ");
                sb.append(dicreteInputs[0]);
                logger.info(sb.toString());
                payload.addMetric("input", Boolean.valueOf(dicreteInputs[0]));
            }
            if (this.registeraddr > 0) {
                int[] analogInputs = this.protocolDevice.readInputRegisters(this.slaveAddr, this.registeraddr, 1);
                short val = (short) analogInputs[0];
                StringBuilder sb = new StringBuilder().append("Register ").append(this.registeraddr).append(" = ");
                sb.append(val);
                logger.info(sb.toString());
                payload.addMetric("register", Integer.valueOf(analogInputs[0]));
            }

            // time to publish ?
            long elapsed = System.currentTimeMillis() - this.startPublish;
            if (elapsed > this.publishInterval * 1000) {
                this.startPublish = System.currentTimeMillis();
                if (!payload.metrics().isEmpty()) {

                    // Timestamp the message
                    payload.setTimestamp(new Date());

                    KuraMessage message = new KuraMessage(payload);

                    // Publish the message
                    try {
                        String messageId = this.cloudPublisher.publish(message);
                        logger.info("Published message: {} with ID: {}", payload, messageId);
                    } catch (Exception e) {
                        logger.error("Cannot publish message {}", message, e);
                    }
                }
            }
        } catch (ModbusProtocolException e) {
            logger.error("ModbusProtocolException :  {}", e.getMessage());
        }
    }

    private void configureDevice() throws ModbusProtocolException {
        if (this.protocolDevice != null) {
            this.protocolDevice.disconnect();

            this.protocolDevice.configureConnection(this.modbusProperties);

            this.configured = true;
        }
    }

    private Properties getModbusProperties(ModbusExampleOCD ocd) {

        Properties prop = new Properties();

        String modbusProtocol = null;

        modbusProtocol = ocd.modbus_protocol();
        logger.info("Configuring Modbus with protocol: {}", modbusProtocol);
        prop.setProperty("connectionType", modbusProtocol);

        String slave = ocd.slave_address();
        prop.setProperty("slaveAddr", slave);

        boolean isTCP = "TCP-RTU".equals(modbusProtocol) || "TCP/IP".equals(modbusProtocol);
        if (isTCP) {
            int iport = ocd.tcp_port();
            prop.setProperty("ethport", String.valueOf(iport));
            prop.setProperty("ipAddress", ocd.ip_address());
            logger.info("Configuring TCP connection - IP: {}, Port: {}", ocd.ip_address(), iport);
        } else {
            String portName = null;
            String baudRate = null;
            String bitsPerWord = null;
            String stopBits = null;
            String parity = null;

            portName = ocd.serial_port();

            baudRate = ocd.serial_baudrate();

            bitsPerWord = ocd.serial_data_bits();

            stopBits = ocd.serial_stop_bits();

            parity = ocd.serial_parity();

            logger.info("Configuring Serial connection - Port: {}, Baud: {}, Data bits: {}, Stop bits: {}, Parity: {}",
                    portName, baudRate, bitsPerWord, stopBits, parity);

            if (portName == null) {
                logger.error("Serial port name is null!");
                return null;
            }
            if (baudRate == null) {
                baudRate = "9600";
            }
            if (stopBits == null) {
                stopBits = "1";
            }
            if (parity == null) {
                parity = "0";
            }
            if (bitsPerWord == null) {
                bitsPerWord = "8";
            }

            prop.setProperty("port", portName);
            prop.setProperty("exclusive", "false");
            prop.setProperty("baudRate", baudRate);
            prop.setProperty("stopBits", stopBits);
            prop.setProperty("parity", parity);
            prop.setProperty("bitsPerWord", bitsPerWord);
        }
        prop.setProperty("mode", "0");
        prop.setProperty("transmissionMode", "RTU");
        prop.setProperty("respTimeout", "1000");
        this.slaveAddr = Integer.valueOf(slave);

        logger.info("Final Modbus properties: {}", prop);
        logger.info("Slave address: {}", this.slaveAddr);

        return prop;
    }

    // ----------------------------------------------------------------
    //
    // Cloud Application Callback Methods
    //
    // ----------------------------------------------------------------
    @Override
    public void onConnectionEstablished() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onConnectionLost() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onMessageConfirmed(String messageId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDisconnected() {
        // TODO Auto-generated method stub

    }

}
