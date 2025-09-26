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
 *******************************************************************************/
package org.eclipse.kura.example.serial.publisher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.kura.cloudconnection.message.KuraMessage;
import org.eclipse.kura.cloudconnection.publisher.CloudPublisher;
import org.eclipse.kura.cloudconnection.subscriber.CloudSubscriber;
import org.eclipse.kura.cloudconnection.subscriber.listener.CloudSubscriberListener;
import org.eclipse.kura.comm.CommConnection;
import org.eclipse.kura.comm.CommURI;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.message.KuraPayload;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.io.ConnectionFactory;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, //
        enabled = true, //
        name = "org.eclipse.kura.example.serial.publisher.ExampleSerialPublisher", //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        service = { ConfigurableComponent.class } //
)
@Designate(ocd = ExampleSerialPublisherOCD.class, factory = false)
public class ExampleSerialPublisher implements ConfigurableComponent, CloudSubscriberListener {

    private static final Logger logger = LoggerFactory.getLogger(ExampleSerialPublisher.class);

    private ConnectionFactory connectionFactory;

    private CommConnection commConnection;
    private InputStream commIs;
    private OutputStream commOs;

    private final ScheduledExecutorService worker;
    private Future<?> handle;

    private CloudPublisher cloudPublisher;
    private CloudSubscriber cloudSubscriber;

    // ----------------------------------------------------------------
    //
    // Dependencies
    //
    // ----------------------------------------------------------------

    public ExampleSerialPublisher() {
        super();
        this.worker = Executors.newSingleThreadScheduledExecutor();
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

    @Reference(name = "CloudSubscriber", //
            policy = ReferencePolicy.DYNAMIC, //
            cardinality = ReferenceCardinality.OPTIONAL, //
            unbind = "unsetCloudSubscriber" //
    )
    public void setCloudSubscriber(final CloudSubscriber cloudSubscriber) {
        this.cloudSubscriber = cloudSubscriber;
        this.cloudSubscriber.registerCloudSubscriberListener(ExampleSerialPublisher.this);
    }

    public void unsetCloudSubscriber(final CloudSubscriber cloudSubscriber) {
        this.cloudSubscriber.unregisterCloudSubscriberListener(ExampleSerialPublisher.this);
        this.cloudSubscriber = null;
    }

    @Reference(name = "ConnectionFactory", //
            policy = ReferencePolicy.STATIC, //
            cardinality = ReferenceCardinality.MANDATORY, //
            unbind = "unsetConnectionFactory" //
    )
    public void setConnectionFactory(final ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void unsetConnectionFactory(final ConnectionFactory connectionFactory) {
        this.connectionFactory = null;
    }

    // ----------------------------------------------------------------
    //
    // Activation APIs
    //
    // ----------------------------------------------------------------

    @Activate
    protected void activate(final ExampleSerialPublisherOCD ocd) {
        ExampleSerialPublisher.logger.info("Activating ExampleSerialPublisher...");

        // get the mqtt client for this application
        try {

            // Don't subscribe because these are handled by the default
            // subscriptions and we don't want to get messages twice
            doUpdate(ocd);
        } catch (final Exception e) {
            ExampleSerialPublisher.logger.error("Error during component activation", e);
            throw new ComponentException(e);
        }
        ExampleSerialPublisher.logger.info("Activating ExampleSerialPublisher... Done.");
    }

    @Deactivate
    protected void deactivate(final ComponentContext componentContext) {
        ExampleSerialPublisher.logger.info("Deactivating ExampleSerialPublisher...");

        this.handle.cancel(true);

        // shutting down the worker and cleaning up the properties
        this.worker.shutdownNow();

        closePort();

        ExampleSerialPublisher.logger.info("Deactivating ExampleSerialPublisher... Done.");
    }

    @Modified
    public void updated(final ExampleSerialPublisherOCD ocd) {
        ExampleSerialPublisher.logger.info("Updated ExampleSerialPublisher...");

        // try to kick off a new job
        doUpdate(ocd);
        ExampleSerialPublisher.logger.info("Updated ExampleSerialPublisher... Done.");
    }

    // ----------------------------------------------------------------
    //
    // Private Methods
    //
    // ----------------------------------------------------------------

    /**
     * Called after a new set of properties has been configured on the service
     */
    private void doUpdate(final ExampleSerialPublisherOCD ocd) {
        try {

            // cancel a current worker handle if one if active
            if (this.handle != null) {
                this.handle.cancel(true);
            }

            closePort();

            openPort(ocd);

            this.handle = this.worker.submit(new Runnable() {

                @Override
                public void run() {
                    doSerial(ocd);
                }
            });
        } catch (final Throwable t) {
            ExampleSerialPublisher.logger.error("Unexpected Throwable", t);
        }
    }

    private void openPort(final ExampleSerialPublisherOCD ocd) {
        final String port = ocd.serial_device();

        if (port == null) {
            ExampleSerialPublisher.logger.info("Port name not configured");
            return;
        }

        final String uri = new CommURI.Builder(port).withBaudRate(Integer.parseInt(ocd.serial_baudrate()))
                .withDataBits(Integer.parseInt(ocd.serial_data_bits()))
                .withStopBits(Integer.parseInt(ocd.serial_stop_bits())).withParity(retrieveParity(ocd.serial_parity()))
                .withOpenTimeout(1000).build().toString();

        try {
            this.commConnection = (CommConnection) this.connectionFactory.createConnection(uri, 1, false);
            this.commIs = this.commConnection.openInputStream();
            this.commOs = this.commConnection.openOutputStream();

            ExampleSerialPublisher.logger.info("{} open", port);
        } catch (final IOException e) {
            ExampleSerialPublisher.logger.error("Failed to open port", e);
            cleanupPort();
        }
    }

    private int retrieveParity(final String sParity) {
        return switch (sParity) {
        case "none" -> CommURI.PARITY_NONE;
        case "odd" -> CommURI.PARITY_ODD;
        case "even" -> CommURI.PARITY_EVEN;
        default -> throw new IllegalArgumentException("Invalid parity: " + sParity);
        };
    }

    private void cleanupPort() {
        if (this.commIs != null) {
            try {
                ExampleSerialPublisher.logger.info("Closing port input stream...");
                this.commIs.close();
                ExampleSerialPublisher.logger.info("Closed port input stream");
            } catch (final IOException e) {
                ExampleSerialPublisher.logger.error("Cannot close port input stream", e);
            }
            this.commIs = null;
        }
        if (this.commOs != null) {
            try {
                ExampleSerialPublisher.logger.info("Closing port output stream...");
                this.commOs.close();
                ExampleSerialPublisher.logger.info("Closed port output stream");
            } catch (final IOException e) {
                ExampleSerialPublisher.logger.error("Cannot close port output stream", e);
            }
            this.commOs = null;
        }
        if (this.commConnection != null) {
            try {
                ExampleSerialPublisher.logger.info("Closing port...");
                this.commConnection.close();
                ExampleSerialPublisher.logger.info("Closed port");
            } catch (final IOException e) {
                ExampleSerialPublisher.logger.error("Cannot close port", e);
            }
            this.commConnection = null;
        }
    }

    private void closePort() {
        cleanupPort();
    }

    private void doSerial(final ExampleSerialPublisherOCD ocd) {
        if (this.commIs == null) {
            return;
        }

        final boolean echo = ocd.serial_echo();
        final StringBuilder lineBuffer = new StringBuilder();

        try (InputStream inputStream = this.commIs) {
            final byte[] buffer = new byte[1024];

            while (!Thread.currentThread().isInterrupted() && this.commIs != null) {
                final int bytesRead = inputStream.read(buffer);

                if (bytesRead > 0) {
                    processReceivedData(buffer, bytesRead, echo, lineBuffer);
                }
            }
        } catch (final IOException e) {
            ExampleSerialPublisher.logger.error("Error reading from serial port", e);
        }
    }

    private void processReceivedData(final byte[] buffer, final int length, final boolean echo,
            final StringBuilder lineBuffer) {
        try {
            for (int i = 0; i < length; i++) {
                final char ch = (char) buffer[i];

                // Echo se richiesto
                if (echo && this.commOs != null) {
                    this.commOs.write(ch);
                }

                // Gestione fine linea
                if (ch == '\r') {  // Carriage Return
                    publishLine(lineBuffer.toString());
                    lineBuffer.setLength(0);  // Reset buffer
                } else if (ch != '\n') {  // Ignora Line Feed
                    lineBuffer.append(ch);
                }
            }

            // Flush echo output
            if (echo && this.commOs != null) {
                this.commOs.flush();
            }
        } catch (final IOException e) {
            ExampleSerialPublisher.logger.error("Error processing serial data", e);
        }
    }

    private void publishLine(final String line) {
        if (this.cloudPublisher == null) {
            ExampleSerialPublisher.logger.debug("No cloud publisher available");
            return;
        }

        if (line.trim().isEmpty()) {
            return; // Non pubblicare linee vuote
        }

        final KuraPayload payload = new KuraPayload();
        payload.setTimestamp(new Date());
        payload.addMetric("line", line);

        final KuraMessage message = new KuraMessage(payload);

        try {
            this.cloudPublisher.publish(message);
            ExampleSerialPublisher.logger.debug("Published: {}", line);
        } catch (final Exception e) {
            ExampleSerialPublisher.logger.error("Failed to publish message", e);
        }
    }

    @Override
    public void onMessageArrived(final KuraMessage message) {
        // TODO Auto-generated method stub

    }
}
