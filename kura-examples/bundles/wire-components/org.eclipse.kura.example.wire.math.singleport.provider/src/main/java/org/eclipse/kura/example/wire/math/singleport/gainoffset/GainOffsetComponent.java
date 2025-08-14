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

package org.eclipse.kura.example.wire.math.singleport.gainoffset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.kura.configuration.ConfigurableComponent;
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
        name = "org.eclipse.kura.example.wire.math.singleport.gainoffset.GainOffsetComponent", //
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
@Designate(ocd = GainOffsetComponentOCD.class, factory = true)
public class GainOffsetComponent implements WireEmitter, WireReceiver, ConfigurableComponent {

    private static final Logger logger = LoggerFactory.getLogger(GainOffsetComponent.class);

    private WireHelperService wireHelperService;
    private WireSupport wireSupport;

    private GainOffsetComponentOptions gainOpts;

    @Reference(name = "WireHelperService", //
            policy = ReferencePolicy.STATIC, //
            cardinality = ReferenceCardinality.MANDATORY, //
            unbind = "unbindWireHelperService" //
    )
    public void bindWireHelperService(final WireHelperService wireHelperService) {
        this.wireHelperService = wireHelperService;
    }

    public void unbindWireHelperService(final WireHelperService wireHelperService) {
        if (this.wireHelperService != null) {
            this.wireHelperService = null;
        }
    }

    @Activate
    public void activate(final ComponentContext componentContext, final GainOffsetComponentOCD ocd) {
        this.wireSupport = this.wireHelperService.newWireSupport(this,
                (ServiceReference<WireComponent>) componentContext.getServiceReference());
        updated(ocd);
    }

    @Modified
    public void updated(final GainOffsetComponentOCD ocd) {
        try {
            this.gainOpts = new GainOffsetComponentOptions(ocd);
        } catch (final Exception e) {
            logger.warn("Invalid configuration, please review", e);
            this.gainOpts = null;
        }
    }

    @Deactivate
    public void deactivate() {
        logger.info("Deactivating gain offset component...");
        logger.info("Deactivating gain offset component...Done");
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
        if (gainOpts == null) {
            logger.warn("Invalid configuration, please review");
        }
        final List<WireRecord> inputRecords = wireEnvelope.getRecords();
        final List<WireRecord> records = new ArrayList<>(inputRecords.size());
        for (final WireRecord inputWireRecord : inputRecords) {
            records.add(processRecord(inputWireRecord));
        }
        this.wireSupport.emit(records);
    }

    private WireRecord processRecord(final WireRecord inputWireRecord) {
        final Map<String, TypedValue<?>> inputProperties = inputWireRecord.getProperties();
        final Map<String, TypedValue<?>> outProperties = new HashMap<>();
        if (this.gainOpts.shouldEmitReceivedProperties()) {
            outProperties.putAll(inputProperties);
        }
        for (final GainOffsetEntry e : this.gainOpts.getEntries()) {
            final String propertyName = e.getPropertyName();
            final TypedValue<?> typedValue = inputProperties.get(propertyName);
            if (typedValue == null) {
                continue;
            }
            final Object value = typedValue.getValue();
            if (!(value instanceof Number)) {
                logger.warn("Invalid property value: {}={}", propertyName, typedValue);
                continue;
            }
            outProperties.put(propertyName,
                    TypedValues.newDoubleValue(((Number) value).doubleValue() * e.getGain() + e.getOffset()));
        }
        return new WireRecord(outProperties);
    }
}
