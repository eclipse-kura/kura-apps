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

package org.eclipse.kura.example.wire.math.singleport.median;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.example.wire.math.singleport.RunningMedian;
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
        name = "org.eclipse.kura.example.wire.math.singleport.median.MedianComponent", //
        property = { //
                "input.cardinality.minimum:Integer=1", //
                "input.cardinality.maximum:Integer=1", //
                "input.cardinality.default:Integer=1", //
                "output.cardinality.minimum:Integer=1", //
                "output.cardinality.maximum:Integer=1", //
                "output.cardinality.default:Integer=1", //
                "kura.ui.service.hide:Boolean=true" //
        } //
)
@Designate(ocd = MedianComponentOCD.class, factory = true)
public class MedianComponent implements WireEmitter, WireReceiver, ConfigurableComponent, UnaryOperator<TypedValue<?>> {

    private static final Logger logger = LoggerFactory.getLogger(MedianComponent.class);

    private WireHelperService wireHelper;
    private WireSupport wireSupp;
    protected MedianComponentOptions medianOptions;

    private RunningMedian<Double> runningMedian;

    @Reference(name = "WireHelperService", //
            policy = ReferencePolicy.STATIC, //
            cardinality = ReferenceCardinality.MANDATORY, //
            unbind = "unbindWireHelperService" //
    )
    public void bindWireHelperService(final WireHelperService wireHelperService) {
        this.wireHelper = wireHelperService;
    }

    public void unbindWireHelperService(final WireHelperService wireHelperService) {
        this.wireHelper = null;
    }

    @Deactivate
    public void deactivate() {
        logger.info("Deactivating median component...");
        logger.info("Deactivating median component...Done");
    }

    @Activate
    public void activate(final ComponentContext componentContext, final MedianComponentOCD ocd) {
        this.wireSupp = this.wireHelper.newWireSupport(this,
                (ServiceReference<WireComponent>) componentContext.getServiceReference());
        updated(ocd);
    }

    @Modified
    public void updated(final MedianComponentOCD ocd) {
        this.medianOptions = getOptions(ocd);
        init();
    }

    protected MedianComponentOptions getOptions(final MedianComponentOCD ocd) {
        return new MedianComponentOptions(ocd);
    }

    @Override
    public Object polled(final Wire wire) {
        return wireSupp.polled(wire);
    }

    @Override
    public void consumersConnected(final Wire[] wires) {
        wireSupp.consumersConnected(wires);
    }

    @Override
    public void updated(final Wire wire, final Object value) {
        wireSupp.updated(wire, value);
    }

    @Override
    public void producersConnected(final Wire[] wires) {
        wireSupp.producersConnected(wires);
    }

    @Override
    public void onWireReceive(final WireEnvelope wireEnvelope) {
        final List<WireRecord> records = wireEnvelope.getRecords();
        if (records.isEmpty()) {
            logger.warn("Received empty envelope");
            return;
        }
        final Map<String, TypedValue<?>> properties = records.get(0).getProperties();
        final TypedValue<?> operand = properties.get(this.medianOptions.getOperandName());
        if (operand == null) {
            logger.warn("Missing operand");
            return;
        }
        if (!(operand.getValue() instanceof Number)) {
            logger.warn("Not a number: {}", operand);
            return;
        }
        final TypedValue<?> result = this.apply(operand);
        if (this.medianOptions.shouldEmitReceivedProperties().booleanValue()) {
            final Map<String, TypedValue<?>> resultProperties = new HashMap<>(properties);
            resultProperties.put(this.medianOptions.getResultName(), result);
            this.wireSupp.emit(Collections.singletonList(new WireRecord(resultProperties)));
        } else {
            this.wireSupp.emit(Collections.singletonList(
                    new WireRecord(Collections.singletonMap(this.medianOptions.getResultName(), result))));
        }
    }

    protected void init() {
        this.runningMedian = null;
    }

    @Override
    public TypedValue<?> apply(final TypedValue<?> t) {
        if (runningMedian == null) {
            this.runningMedian = new RunningMedian<>(this.medianOptions.getWindowSize());
        }
        final double value = ((Number) t.getValue()).doubleValue();
        this.runningMedian.add(value);
        return TypedValues.newDoubleValue(this.runningMedian.median());
    }

}
