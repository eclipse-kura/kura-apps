/*******************************************************************************
 * Copyright (c) 2025 Eurotech and/or its affiliates and others
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
package org.eclipse.kura.example.wire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

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
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.wireadmin.Consumer;
import org.osgi.service.wireadmin.Producer;
import org.osgi.service.wireadmin.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        enabled = true, //
        name = "org.eclipse.kura.example.wire.WireComponentExample", //
        property = { //
                "input.cardinality.minimum:Integer=1", //
                "input.cardinality.maximum:Integer=1", //
                "input.cardinality.default:Integer=1", //
                "output.cardinality.minimum:Integer=1", //
                "output.cardinality.maximum:Integer=1", //
                "output.cardinality.default:Integer=1", //
                "input.port.names=0=card1", //
                "output.port.names=0=result" //
        },  //
        service = { ConfigurableComponent.class, WireEmitter.class, WireReceiver.class, WireComponent.class,
                Producer.class, Consumer.class } //
)
@Designate(ocd = WireComponentExampleOCD.class, factory = true)
public class WireComponentExample implements ConfigurableComponent, WireEmitter, WireReceiver {

    private static final Logger logger = LoggerFactory.getLogger(WireComponentExample.class);

    private WireComponentExampleOptions options;

    private WireSupport wireSupport;

    @Reference
    private WireHelperService wireHelperService;

    @Reference
    public void setWireHelperService(WireHelperService helperService) {
        this.wireHelperService = helperService;
    }

    /*
     * In the in activate, modified, deactivate methods it is possible to provide
     * the ComponentContext and the ExampleComponentOCD as parameters.
     * All parameters in activate, modified, deactivate are optional and can be
     * removed if not needed
     * 
     * Examples:
     * 
     * public void activate()
     * public void activate(ExampleComponentOCD configuration)
     * public void activate(ComponentContext componentContext, final Map<String, Object> properties, final
     * ExampleComponentOCD configuration)
     */
    @SuppressWarnings("unchecked")
    @Activate
    public void activate(ComponentContext componentContext, WireComponentExampleOCD properties) {
        logger.info("Activating WireComponentExample...");

        this.wireSupport = this.wireHelperService.newWireSupport(this,
                (ServiceReference<WireComponent>) componentContext.getServiceReference());

        updated(properties);

        logger.info("Activated WireComponentExample.");
    }

    @Modified
    public synchronized void updated(WireComponentExampleOCD properties) {
        logger.info("Updating WireComponentExample...");

        logger.debug("Updating with properties: {}", properties);
        this.options = new WireComponentExampleOptions(properties);

        logger.info("Updated WireComponentExample.");
    }

    @Deactivate
    public synchronized void deactivate() {
        logger.info("Deactivating WireComponentExample...");
        logger.info("Deactivated WireComponentExample.");
    }

    public WireComponentExampleOptions getOptions() {
        return this.options;
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

        AtomicReference<WireRecord> wireRecordReference = new AtomicReference<>();
        AtomicReference<String> channelValueReference = new AtomicReference<>();

        if (wireEnvelope.getRecords().isEmpty()) {
            logger.warn("Envelope must contain at least one channel");
            return;
        }

        wireEnvelope.getRecords().forEach(selectedRecord -> {
            selectedRecord.getProperties().entrySet().stream()
                    .filter(entry -> this.options.getChannelFilterName().equals(entry.getKey())).findAny()
                    .ifPresent(entry -> {
                        wireRecordReference.set(selectedRecord);
                        channelValueReference.set(entry.getValue().getValue().toString());
                    });
        });

        if (Objects.isNull(channelValueReference.get()) || Objects.isNull(wireRecordReference.get())) {
            logger.warn("No match found for channel name {}", this.options.getChannelFilterName());
            return;
        }

        List<WireRecord> emittingRecords = new ArrayList<>();
        Map<String, TypedValue<?>> props = new HashMap<>();
        props.put("Emitted Value",
                TypedValues.newStringValue(String.format("You choose the channel %s, whose value is %s",
                        this.options.getChannelFilterName(), channelValueReference.get())));
        emittingRecords.add(new WireRecord(props));
        this.wireSupport.emit(emittingRecords);

    }

}
