/*******************************************************************************
 * Copyright (c) 2017, 2026 Eurotech and/or its affiliates and others
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
package org.eclipse.kura.internal.misc.cloudcat;

import static org.eclipse.kura.configuration.ConfigurationService.KURA_SERVICE_PID;

import java.util.Map;
import java.util.function.Consumer;
import org.eclipse.kura.cloudconnection.CloudEndpoint;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudCat implements ConfigurableComponent {

    private static final Logger logger = LoggerFactory.getLogger(CloudCat.class);

    private CloudCatOptions options;
    private CloudEndpoint firstCloudEndpoint;
    private CloudEndpoint secondCloudEndpoint;
    private CloudEndpointRelay firstCloudEndpointRelay;
    private CloudEndpointRelay secondCloudEndpointRelay;

    private ServiceTracker<CloudEndpoint, CloudEndpoint> firstCloudEndpointTracker;
    private ServiceTracker<CloudEndpoint, CloudEndpoint> secondCloudEndpointTracker;

    private ComponentContext componentContext;

    private void bindFirstCloudEndpoint(CloudEndpoint firstCloudEndpoint) {
        this.firstCloudEndpoint = firstCloudEndpoint;
        initRelays();
    }

    private void bindSecondCloudEndpoint(CloudEndpoint secondCloudEndpoint) {
        this.secondCloudEndpoint = secondCloudEndpoint;
        initRelays();
    }

    private void unbindFirstCloudEndpoint(CloudEndpoint firstCloudEndpoint) {
        cleanupRelays();
        this.firstCloudEndpoint = null;
    }

    private void unbindSecondCloudEndpoint(CloudEndpoint secondCloudEndpoint) {
        cleanupRelays();
        this.secondCloudEndpoint = null;
    }

    protected void activate(ComponentContext ctx, Map<String, Object> properties) {
        logger.info("Activating {}", ctx.getProperties().get(KURA_SERVICE_PID));
        this.componentContext = ctx;
        init(properties);
    }

    protected void updated(ComponentContext ctx, Map<String, Object> properties) {
        logger.info("Updating {}", ctx.getProperties().get(KURA_SERVICE_PID));
        cleanup();
        init(properties);
    }

    protected void deactivate(ComponentContext ctx) {
        logger.info("Deactivating {}", ctx.getProperties().get(KURA_SERVICE_PID));
        cleanup();
    }

    private void init(Map<String, Object> properties) {
        try {
            this.options = CloudCatOptions.parseOptions(properties);
        } catch (NullPointerException | IllegalArgumentException e) {
            logger.error("Invalid configuration", e);
            return;
        }

        if (!this.options.isRelayEnabled()) {
            logger.info("Relay is disabled");
            return;
        }

        try {
            this.firstCloudEndpointTracker = openCloudEndpointTracker(
                    this.options.getFirstCloudServicePid(),
                    this::bindFirstCloudEndpoint,
                    this::unbindFirstCloudEndpoint);
            this.secondCloudEndpointTracker = openCloudEndpointTracker(
                    this.options.getSecondCloudServicePid(),
                    this::bindSecondCloudEndpoint,
                    this::unbindSecondCloudEndpoint);
        } catch (InvalidSyntaxException e) {
            logger.error("Invalid filter", e);
        }
    }

    private ServiceTracker<CloudEndpoint, CloudEndpoint> openCloudEndpointTracker(
            String cloudEndpointPid, Consumer<CloudEndpoint> bind, Consumer<CloudEndpoint> unbind)
            throws InvalidSyntaxException {
        final BundleContext bundleContext = this.componentContext.getBundleContext();
        final Filter filter = bundleContext.createFilter("(&(" + Constants.OBJECTCLASS + "="
                + CloudEndpoint.class.getName() + ")(" + KURA_SERVICE_PID + "=" + cloudEndpointPid + "))");

        final ServiceTracker<CloudEndpoint, CloudEndpoint> tracker = new ServiceTracker<>(
                bundleContext, filter, new ServiceTrackerCustomizer<CloudEndpoint, CloudEndpoint>() {

                    @Override
                    public CloudEndpoint addingService(ServiceReference<CloudEndpoint> reference) {
                        CloudEndpoint cloudEndpoint = bundleContext.getService(reference);
                        if (cloudEndpoint != null) {
                            bind.accept(cloudEndpoint);
                        }
                        return cloudEndpoint;
                    }

                    @Override
                    public void modifiedService(ServiceReference<CloudEndpoint> reference, CloudEndpoint service) {
                        // Ignore
                    }

                    @Override
                    public void removedService(ServiceReference<CloudEndpoint> reference, CloudEndpoint service) {
                        unbind.accept(service);
                        bundleContext.ungetService(reference);
                    }
                });
        tracker.open();
        return tracker;
    }

    private void initRelays() {
        if (this.firstCloudEndpoint != null
                && this.secondCloudEndpoint != null
                && this.firstCloudEndpointRelay == null
                && this.secondCloudEndpointRelay == null) {
            logger.info("Initializing relay");

            this.firstCloudEndpointRelay = new CloudEndpointRelay(
                    this.firstCloudEndpoint,
                    this.options.getFirstCloudClientAppId(),
                    this.secondCloudEndpoint,
                    this.options.getSecondCloudClientAppId(),
                    this.options.getFirstCloudClientDataSubscriptions(),
                    this.options.getFirstCloudClientControlSubscriptions());

            this.secondCloudEndpointRelay = new CloudEndpointRelay(
                    this.secondCloudEndpoint,
                    this.options.getSecondCloudClientAppId(),
                    this.firstCloudEndpoint,
                    this.options.getFirstCloudClientAppId(),
                    this.options.getSecondCloudClientDataSubscriptions(),
                    this.options.getSecondCloudClientControlSubscriptions());

            this.firstCloudEndpointRelay.start();
            this.secondCloudEndpointRelay.start();
        }
    }

    private void cleanup() {
        if (this.firstCloudEndpointTracker != null) {
            this.firstCloudEndpointTracker.close();
            this.firstCloudEndpointTracker = null;
        }
        if (this.secondCloudEndpointTracker != null) {
            this.secondCloudEndpointTracker.close();
            this.secondCloudEndpointTracker = null;
        }
        cleanupRelays();
    }

    private void cleanupRelays() {
        if (this.firstCloudEndpointRelay != null) {
            this.firstCloudEndpointRelay.stop();
            this.firstCloudEndpointRelay = null;
        }
        if (this.secondCloudEndpointRelay != null) {
            this.secondCloudEndpointRelay.stop();
            this.secondCloudEndpointRelay = null;
        }
    }
}
