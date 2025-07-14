/*******************************************************************************
 * Copyright (c) 2020 Eurotech and/or its affiliates and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *  Eurotech
 ******************************************************************************/
package org.eclipse.kura.example.wire.logic.multiport.provider;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.type.TypedValue;
import org.eclipse.kura.type.TypedValues;
import org.eclipse.kura.wire.WireComponent;
import org.eclipse.kura.wire.WireEmitter;
import org.eclipse.kura.wire.WireEnvelope;
import org.eclipse.kura.wire.WireHelperService;
import org.eclipse.kura.wire.WireRecord;
import org.eclipse.kura.wire.graph.MultiportWireSupport;
import org.eclipse.kura.wire.multiport.MultiportWireReceiver;
import org.osgi.framework.BundleContext;
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

@Component( //
        immediate = true, //
        enabled = true, //
        name = "org.eclipse.kura.wire.LogicalOperators", //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        service = { WireEmitter.class, ConfigurableComponent.class, MultiportWireReceiver.class, Producer.class,
                Consumer.class }, //
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
@Designate(ocd = LogicalComponentOCD.class, factory = true)
public class LogicalComponent implements WireEmitter, ConfigurableComponent, MultiportWireReceiver {

    private static final Logger logger = LoggerFactory.getLogger(LogicalComponent.class);

    private WireHelperService wireHelperService;
    private MultiportWireSupport wireSupport;

    protected LogicalComponentOptions options;
    protected BundleContext context;

    @Reference(name = "WireHelperService", //
            policy = ReferencePolicy.STATIC, //
            cardinality = ReferenceCardinality.OPTIONAL //
    )
    public void bindWireHelperService(final WireHelperService wireHelperService) {
        this.wireHelperService = wireHelperService;
    }

    @SuppressWarnings("unchecked")
    @Activate
    public void activate(final LogicalComponentOCD ocd, ComponentContext componentContext) {
        logger.info("activating...");
        this.wireSupport = (MultiportWireSupport) this.wireHelperService.newWireSupport(this,
                (ServiceReference<WireComponent>) componentContext.getServiceReference());
        logger.info("activated, properties: {}", ocd);
        this.context = componentContext.getBundleContext();
        updated(ocd, componentContext);
        logger.info("activating...done");
    }

    @Modified
    public void updated(LogicalComponentOCD ocd, ComponentContext componentContext) {
        logger.info("updating...");
        this.options = new LogicalComponentOptions(ocd, this.context);
        logger.info("updated, properties: {}", ocd);
        this.options.getPortAggregatorFactory().build(this.wireSupport.getReceiverPorts())
                .onWireReceive(this::onWireReceive);
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

    private Boolean extractOperand(WireEnvelope wireEnvelope, String operandName) {
        final Map<String, TypedValue<?>> properties = wireEnvelope.getRecords().get(0).getProperties();
        return (Boolean) properties.get(operandName).getValue();
    }

    public void onWireReceive(List<WireEnvelope> wireEnvelopes) {
        final Boolean firstOperand = extractOperand(wireEnvelopes.get(0), this.options.getFirstOperandName());
        final Boolean result;
        if (this.options.isUnaryOperator()) {
            result = this.options.getBooleanFunction().apply(firstOperand, null);
        } else {
            result = this.options.getBooleanFunction().apply(firstOperand,
                    extractOperand(wireEnvelopes.get(1), this.options.getSecondOperandName()));
        }
        WireRecord toBeEmitted = new WireRecord(
                Collections.singletonMap(this.options.getResultName(), TypedValues.newBooleanValue(result)));
        this.wireSupport.emit(Collections.singletonList(toBeEmitted));
    }
}
