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

package org.eclipse.kura.example.wire.math.singleport.sqrt;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.example.wire.math.singleport.maximum.MaximumComponentOCD;
import org.eclipse.kura.example.wire.math.singleport.maximum.MaximumComponentOptions;
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
        name = "org.eclipse.kura.example.wire.math.singleport.sqrt.SqrtComponent", //
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
@Designate(ocd = SqrtComponentOCD.class, factory = true)
public class SqrtComponent
        implements WireEmitter, WireReceiver, ConfigurableComponent, Function<TypedValue<?>, TypedValue<?>> {

    private static final Logger logger = LoggerFactory.getLogger(SqrtComponent.class);

    private WireHelperService wireHelperService;
    private WireSupport wireSupport;
    protected MaximumComponentOptions options;

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
    public void activate(ComponentContext componentContext, MaximumComponentOCD ocd) {
        this.wireSupport = this.wireHelperService.newWireSupport(this,
                (ServiceReference<WireComponent>) componentContext.getServiceReference());
        updated(ocd);
    }

    @Modified
    public void updated(MaximumComponentOCD ocd) {
        this.options = getOptions(ocd);
    }

    @Deactivate
    public void deactivate() {
        logger.info("Deactivating...");
        logger.info("Deactivating...Done");
    }

    protected MaximumComponentOptions getOptions(MaximumComponentOCD ocd) {
        return new MaximumComponentOptions(ocd);
    }

    @Override
    public Object polled(Wire wire) {
        return wireSupport.polled(wire);
    }

    @Override
    public void consumersConnected(Wire[] wires) {
        wireSupport.consumersConnected(wires);
    }

    @Override
    public void updated(Wire wire, Object value) {
        wireSupport.updated(wire, value);
    }

    @Override
    public void producersConnected(Wire[] wires) {
        wireSupport.producersConnected(wires);
    }

    @Override
    public void onWireReceive(WireEnvelope wireEnvelope) {
        final List<WireRecord> records = wireEnvelope.getRecords();
        if (records.isEmpty()) {
            logger.warn("Received empty envelope");
            return;
        }
        final Map<String, TypedValue<?>> properties = records.get(0).getProperties();
        final TypedValue<?> operand = properties.get(this.options.getOperandName());
        if (operand == null) {
            logger.warn("Missing operand");
            return;
        }
        if (!(operand.getValue() instanceof Number)) {
            logger.warn("Not a number: {}", operand);
            return;
        }
        final TypedValue<?> result = this.apply(operand);
        if (this.options.shouldEmitReceivedProperties().booleanValue()) {
            final Map<String, TypedValue<?>> resultProperties = new HashMap<>(properties);
            resultProperties.put(this.options.getResultName(), result);
            this.wireSupport.emit(Collections.singletonList(new WireRecord(resultProperties)));
        } else {
            this.wireSupport.emit(Collections
                    .singletonList(new WireRecord(Collections.singletonMap(this.options.getResultName(), result))));
        }
    }

    public TypedValue<?> apply(TypedValue<?> t) {
        Double value = Double.parseDouble(t.getValue().toString());
        return TypedValues.newDoubleValue(Math.sqrt(value.doubleValue()));
    }

}
