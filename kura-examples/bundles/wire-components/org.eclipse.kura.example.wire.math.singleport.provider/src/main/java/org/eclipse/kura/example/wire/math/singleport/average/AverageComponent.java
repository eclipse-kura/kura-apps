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

package org.eclipse.kura.example.wire.math.singleport.average;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.example.wire.math.singleport.RunningAverage;
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
        name = "org.eclipse.kura.example.wire.math.singleport.average.AverageComponent", //
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
@Designate(ocd = AverageComponentOCD.class, factory = true)
public class AverageComponent
        implements WireEmitter, WireReceiver, ConfigurableComponent, UnaryOperator<TypedValue<?>> {

    private static final Logger logger = LoggerFactory.getLogger(AverageComponent.class);

    private WireHelperService wireHelperService;
    private WireSupport wireSupport;
    protected AverageComponentOptions avrgOptions;

    private RunningAverage runningAverage;

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

    @Activate
    public void activate(final ComponentContext componentContext, final AverageComponentOCD ocd) {
        this.wireSupport = this.wireHelperService.newWireSupport(this,
                (ServiceReference<WireComponent>) componentContext.getServiceReference());
        updated(ocd);
    }

    @Modified
    public void updated(final AverageComponentOCD ocd) {
        this.avrgOptions = getAverageOptions(ocd);
        init();
    }

    @Deactivate
    public void deactivate() {
        logger.info("Deactivating average component...");
        logger.info("Deactivating average component...Done");
    }

    protected AverageComponentOptions getAverageOptions(final AverageComponentOCD ocd) {
        return new AverageComponentOptions(ocd);
    }

    @Override
    public Object polled(final Wire wire) {
        return wireSupport.polled(wire);
    }

    @Override
    public void consumersConnected(final Wire[] wires) {
        wireSupport.consumersConnected(wires);
    }

    @Override
    public void updated(final Wire wire, final Object value) {
        wireSupport.updated(wire, value);
    }

    @Override
    public void producersConnected(final Wire[] wires) {
        wireSupport.producersConnected(wires);
    }

    @Override
    public void onWireReceive(final WireEnvelope wireEnvelope) {
        final List<WireRecord> records = wireEnvelope.getRecords();
        if (records.isEmpty()) {
            logger.warn("Received empty envelope");
            return;
        }
        final Map<String, TypedValue<?>> properties = records.get(0).getProperties();
        final TypedValue<?> operand = properties.get(this.avrgOptions.getOperandName());
        if (operand == null) {
            logger.warn("Missing operand");
            return;
        }
        if (!(operand.getValue() instanceof Number)) {
            logger.warn("Not a number: {}", operand);
            return;
        }
        final TypedValue<?> result = this.apply(operand);
        if (this.avrgOptions.shouldEmitReceivedProperties()) {
            final Map<String, TypedValue<?>> resultProperties = new HashMap<>(properties);
            resultProperties.put(this.avrgOptions.getResultName(), result);
            this.wireSupport.emit(Collections.singletonList(new WireRecord(resultProperties)));
        } else {
            this.wireSupport.emit(Collections
                    .singletonList(new WireRecord(Collections.singletonMap(this.avrgOptions.getResultName(), result))));
        }
    }

    protected void init() {
        this.runningAverage = null;
    }

    @Override
    public TypedValue<?> apply(final TypedValue<?> t) {
        if (runningAverage == null) {
            this.runningAverage = new RunningAverage(this.avrgOptions.getWindowSize());
        }
        final double value = ((Number) t.getValue()).doubleValue();
        return TypedValues.newDoubleValue(this.runningAverage.updateAndGet(value));
    }

}
