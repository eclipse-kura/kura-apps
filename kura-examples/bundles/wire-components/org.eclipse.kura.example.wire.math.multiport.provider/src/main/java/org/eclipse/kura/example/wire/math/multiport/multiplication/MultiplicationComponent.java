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

package org.eclipse.kura.example.wire.math.multiport.multiplication;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.example.wire.math.multiport.AbstractDualportMathComponent;
import org.eclipse.kura.example.wire.math.multiport.ParserOCD;
import org.eclipse.kura.type.TypedValue;
import org.eclipse.kura.type.TypedValues;
import org.eclipse.kura.wire.WireComponent;
import org.eclipse.kura.wire.WireEmitter;
import org.eclipse.kura.wire.WireHelperService;
import org.eclipse.kura.wire.multiport.MultiportWireReceiver;
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

@Component(immediate = true, //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        service = { ConfigurableComponent.class, WireComponent.class, Producer.class, Consumer.class,
                MultiportWireReceiver.class, WireEmitter.class }, //
        enabled = true, //
        name = "org.eclipse.kura.example.wire.math.multiport.multiplication.MultiplicationComponent", //
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
@Designate(ocd = MultiplicationComponentOCD.class, factory = true)
public class MultiplicationComponent extends AbstractDualportMathComponent {

    @Reference( //
            name = "wireHelperService", //
            policy = ReferencePolicy.STATIC, //
            cardinality = ReferenceCardinality.MANDATORY, //
            unbind = "unbindWireHelperService" //
    )
    public void bindWireHelperService(final WireHelperService wireHelperService) {
        this.wireHelperService = wireHelperService;
    }

    @Activate
    public void activate(ComponentContext context, MultiplicationComponentOCD ocd) {
        super.activate(new ParserOCD(ocd).toPropertiesMap(), context);
    }

    @Modified
    public void updated(ComponentContext context, MultiplicationComponentOCD ocd) {
        super.updated(new ParserOCD(ocd).toPropertiesMap());
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }

    public void unbindWireHelperService(final WireHelperService wireHelperService) {
        this.wireHelperService = null;
    }

    @Override
    public TypedValue<?> apply(TypedValue<?> t, TypedValue<?> u) {
        double firstOperand = ((Number) t.getValue()).doubleValue();
        double secondOperand = ((Number) u.getValue()).doubleValue();
        return TypedValues.newDoubleValue(firstOperand * secondOperand);
    }
}
