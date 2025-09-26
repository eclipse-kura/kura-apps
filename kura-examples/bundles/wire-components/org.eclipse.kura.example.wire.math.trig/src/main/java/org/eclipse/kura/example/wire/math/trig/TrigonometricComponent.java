/*******************************************************************************
 * Copyright (c) 2020, 2025 Eurotech and/or its affiliates and others
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

package org.eclipse.kura.example.wire.math.trig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.type.DoubleValue;
import org.eclipse.kura.type.TypedValue;
import org.eclipse.kura.type.TypedValues;
import org.eclipse.kura.wire.WireComponent;
import org.eclipse.kura.wire.WireEmitter;
import org.eclipse.kura.wire.WireEnvelope;
import org.eclipse.kura.wire.WireHelperService;
import org.eclipse.kura.wire.WireReceiver;
import org.eclipse.kura.wire.WireRecord;
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
        name = "org.eclipse.kura.example.wire.math.trig.TrigonometricComponent", //
        property = { //
                "input.cardinality.minimum:Integer=2", //
                "input.cardinality.maximum:Integer=2", //
                "input.cardinality.default:Integer=2", //
                "output.cardinality.minimum:Integer=1", //
                "output.cardinality.maximum:Integer=1", //
                "output.cardinality.default:Integer=1", //
                "kura.ui.service.hide:Boolean=true" //
        } //
)
@Designate(ocd = TrigonometricComponentOCD.class, factory = true)
public class TrigonometricComponent implements WireEmitter, ConfigurableComponent, WireReceiver {

    private static final Logger logger = LoggerFactory.getLogger(TrigonometricComponent.class);

    private WireHelperService wireHelperService;

    protected TrigonometricComponentOptions options;
    private WireSupport wireSupport;

    @Reference(name = "WireHelperService", //
            policy = ReferencePolicy.STATIC, //
            cardinality = ReferenceCardinality.MANDATORY //
    )
    public void bindWireHelperService(final WireHelperService wireHelperService) {
        this.wireHelperService = wireHelperService;
    }

    @SuppressWarnings("unchecked")
    @Activate
    public void activate(ComponentContext componentContext, TrigonometricComponentOCD ocd) {
        logger.info("activating...");
        this.wireSupport = this.wireHelperService.newWireSupport(this,
                (ServiceReference<WireComponent>) componentContext.getServiceReference());
        updated(ocd);
        logger.info("activating...done");
    }

    @Modified
    public void updated(TrigonometricComponentOCD ocd) {
        logger.info("updating...");
        this.options = new TrigonometricComponentOptions(ocd);
        logger.info("updated, properties: {}", ocd);
        logger.info("updating...done");
    }

    @Deactivate
    public synchronized void deactivate() {
        logger.info("deactivating...");
        logger.info("deactivating...done");
    }

    @Override
    public Object polled(Wire wire) {
        return this.wireSupport.polled(wire);
    }

    @Override
    public void consumersConnected(Wire[] wires) {
        this.wireSupport.consumersConnected(wires);
    }

    @Override
    public void updated(Wire wire, Object value) {
        this.wireSupport.updated(wire, value);
    }

    @Override
    public void producersConnected(Wire[] wires) {
        this.wireSupport.producersConnected(wires);
    }

    @Override
    public void onWireReceive(WireEnvelope wireEnvelope) {
        final Map<String, TypedValue<?>> properties = wireEnvelope.getRecords().get(0).getProperties();
        final Double parameter = ((Number) properties.get(this.options.getParameterName()).getValue()).doubleValue();
        final Double result = this.options.getTrigonometricFunction().apply(parameter);
        if (result != null) {
            if (this.options.shouldEmitReceivedProperties()) {
                final Map<String, TypedValue<?>> resultProperties = new HashMap<>(properties);
                resultProperties.put(this.options.getResultName(), new DoubleValue(result));
                this.wireSupport.emit(Collections.singletonList(new WireRecord(resultProperties)));
            } else {
                this.wireSupport.emit(Collections.singletonList(new WireRecord(
                        Collections.singletonMap(this.options.getResultName(), new DoubleValue(result)))));
            }
            final WireRecord toBeEmitted = new WireRecord(
                    Collections.singletonMap(this.options.getResultName(), TypedValues.newDoubleValue(result)));
            this.wireSupport.emit(Collections.singletonList(toBeEmitted));
        }
    }
}
