/*******************************************************************************
 * Copyright (c) 2018, 2020 Eurotech and/or its affiliates and others
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
package org.eclipse.kura.wire.devel.sink;

import static java.util.Objects.isNull;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.wire.WireComponent;
import org.eclipse.kura.wire.WireEnvelope;
import org.eclipse.kura.wire.WireHelperService;
import org.eclipse.kura.wire.WireReceiver;
import org.eclipse.kura.wire.WireSupport;
import org.eclipse.kura.wire.devel.driver.dummy.DummyDriverOCD;
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
import org.osgi.service.wireadmin.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        service = { ConfigurableComponent.class, WireComponent.class, WireReceiver.class, Consumer.class }, //
        enabled = true, //
        name = "org.eclipse.kura.wire.devel.sink.Sink" //
)
@Designate(ocd = DummyDriverOCD.class, factory = true)
public class Sink implements WireReceiver, ConfigurableComponent {

    private static final Logger logger = LoggerFactory.getLogger(Sink.class);

    private WireHelperService wireHelperService;
    private WireSupport wireSupport;

    private boolean measureTimings;
    private long lastTimestamp;

    private String kuraServicePid;

    @SuppressWarnings("unchecked")
    @Activate
    public void activate(final ComponentContext context, SinkOCD ocd) {
        logger.info("activating...");

        this.kuraServicePid = (String) context.getProperties().get(ConfigurationService.KURA_SERVICE_PID);
        this.wireSupport = wireHelperService.newWireSupport(this,
                (ServiceReference<WireComponent>) context.getServiceReference());
        updated(ocd);

        logger.info("activating...done");
    }

    @Deactivate
    public void deactivate() {
        logger.info("deactivating...");
        logger.info("deactivating...done");
    }

    @Modified
    public void updated(final SinkOCD ocd) {
        logger.info("updating...");

        this.measureTimings = ocd.measure_timings();

        logger.info("updating...done");
    }

    @Reference(name = "WireHelperService", //
            policy = ReferencePolicy.STATIC, //
            cardinality = ReferenceCardinality.MANDATORY, //
            unbind = "unbindWireHelperService" //
    )
    public void bindWireHelperService(final WireHelperService wireHelperService) {
        if (isNull(this.wireHelperService)) {
            this.wireHelperService = wireHelperService;
        }
    }

    public void unbindWireHelperService(final WireHelperService wireHelperService) {
        if (this.wireHelperService == wireHelperService) {
            this.wireHelperService = null;
        }
    }

    @Override
    public void updated(final Wire wire, final Object value) {
        this.wireSupport.updated(wire, value);
    }

    @Override
    public void producersConnected(final Wire[] wires) {
        this.wireSupport.producersConnected(wires);
    }

    @Override
    public void onWireReceive(final WireEnvelope wireEnvelope) {
        if (measureTimings) {
            final long currentTimestamp = System.currentTimeMillis();
            if (lastTimestamp != 0) {
                long diff = currentTimestamp - lastTimestamp;
                logger.info("{}: {} ms", this.kuraServicePid, diff);
            }
            lastTimestamp = currentTimestamp;
        }
    }
}
