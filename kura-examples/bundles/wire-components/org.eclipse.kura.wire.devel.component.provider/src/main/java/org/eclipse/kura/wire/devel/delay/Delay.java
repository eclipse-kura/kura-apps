/*******************************************************************************
 * Copyright (c) 2018, 2025 Eurotech and/or its affiliates and others
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
package org.eclipse.kura.wire.devel.delay;

import java.security.SecureRandom;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.wire.WireComponent;
import org.eclipse.kura.wire.WireEmitter;
import org.eclipse.kura.wire.WireEnvelope;
import org.eclipse.kura.wire.WireHelperService;
import org.eclipse.kura.wire.WireReceiver;
import org.eclipse.kura.wire.WireSupport;
import org.osgi.framework.ServiceReference;
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
import org.osgi.service.wireadmin.Consumer;
import org.osgi.service.wireadmin.Producer;
import org.osgi.service.wireadmin.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        service = { ConfigurableComponent.class, WireComponent.class, Producer.class, Consumer.class,
                WireReceiver.class, WireEmitter.class }, //
        enabled = true, //
        name = "org.eclipse.kura.wire.devel.delay.Delay" //
)
@Designate(ocd = DelayOCD.class, factory = true)
public class Delay implements WireEmitter, WireReceiver, ConfigurableComponent {

    private static final Logger logger = LoggerFactory.getLogger(Delay.class);

    private WireHelperService wireHelperService;
    private WireSupport wireSupport;

    private final SecureRandom random = new SecureRandom();
    private int delayAverage;
    private int delayStdDev;

    @Reference(name = "WireHelperService", //
            policy = ReferencePolicy.STATIC, //
            cardinality = ReferenceCardinality.MANDATORY, //
            unbind = "unbindWireHelperService" //
    )
    public void bindWireHelperService(final WireHelperService wireHelperService) {
        this.wireHelperService = wireHelperService;
    }

    public void unbindWireHelperService(final WireHelperService wireHelperService) {
        this.wireHelperService = null;
    }

    @SuppressWarnings("unchecked")
    @Activate
    public void activate(final ComponentContext context, final DelayOCD ocd) {
        logger.info("acitvating..");

        wireSupport = this.wireHelperService.newWireSupport(this,
                (ServiceReference<WireComponent>) context.getServiceReference());

        updated(ocd);

        logger.info("activating...done");
    }

    @Deactivate
    public void deactivate() {
        logger.info("deactivating..");
        logger.info("deactivating...done");
    }

    @Modified
    public void updated(final DelayOCD ocd) {
        logger.info("updating..");

        this.delayAverage = ocd.delay_average();
        this.delayStdDev = ocd.delay_standard_deviation();

        logger.info("updating...done");
    }

    @Override
    public void onWireReceive(final WireEnvelope wireEnvelope) {
        final long delayMs = (long) (random.nextGaussian() * delayStdDev + delayAverage);

        if (delayMs > 0) {

            logger.info("sleeping for {} milliseconds", delayMs);

            try {
                Thread.sleep(delayMs);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        wireSupport.emit(wireEnvelope.getRecords());
    }

    @Override
    public Object polled(final Wire wire) {
        return this.wireSupport.polled(wire);
    }

    @Override
    public void consumersConnected(final Wire[] wires) {
        this.wireSupport.consumersConnected(wires);
    }

    @Override
    public void updated(final Wire wire, final Object value) {
        this.wireSupport.updated(wire, value);
    }

    @Override
    public void producersConnected(final Wire[] wires) {
        this.wireSupport.producersConnected(wires);
    }
}
