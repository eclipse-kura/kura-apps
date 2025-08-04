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

import java.util.function.BiFunction;

import org.eclipse.kura.wire.graph.BarrierAggregatorFactory;
import org.eclipse.kura.wire.graph.CachingAggregatorFactory;
import org.eclipse.kura.wire.graph.PortAggregatorFactory;
import org.osgi.framework.BundleContext;

public class LogicalComponentOptions {

    public enum OperatorOption {
        AND,
        OR,
        XOR,
        NOR,
        NAND,
        NOT
    }

    private final String firstOperandName;
    private final String secondOperandName;
    private final String resultName;
    private final BiFunction<Boolean, Boolean, Boolean> booleanFunction;
    private final OperatorOption operator;

    private final PortAggregatorFactory portAggregatorFactory;

    public LogicalComponentOptions(LogicalComponentOCD ocd, BundleContext context) {
        this.operator = OperatorOption.valueOf(ocd.logical_operator());
        this.firstOperandName = ocd.operand_name_1();
        this.secondOperandName = ocd.operand_name_2();
        this.resultName = ocd.result_name();
        this.booleanFunction = getLogicalFunction(this.operator);

        final boolean useBarrier = ocd.barrier();

        if (useBarrier && !OperatorOption.NOT.equals(this.operator)) {
            this.portAggregatorFactory = context
                    .getService(context.getServiceReference(BarrierAggregatorFactory.class));
        } else {
            this.portAggregatorFactory = context
                    .getService(context.getServiceReference(CachingAggregatorFactory.class));
        }
    }

    private BiFunction<Boolean, Boolean, Boolean> getLogicalFunction(OperatorOption op) {
        switch (op) {
        case OR:
            return (t, u) -> t || u;
        case NOR:
            return (t, u) -> !(t || u);
        case NAND:
            return (t, u) -> !(t && u);
        case XOR:
            return (t, u) -> t ^ u;
        case NOT:
            return (t, u) -> !t;
        case AND:
        default:
            return (t, u) -> t && u;
        }
    }

    public String getFirstOperandName() {
        return this.firstOperandName;
    }

    public String getSecondOperandName() {
        return this.secondOperandName;
    }

    public String getResultName() {
        return this.resultName;
    }

    public PortAggregatorFactory getPortAggregatorFactory() {
        return this.portAggregatorFactory;
    }

    public BiFunction<Boolean, Boolean, Boolean> getBooleanFunction() {
        return this.booleanFunction;
    }

    public OperatorOption getOperator() {
        return this.operator;
    }

    public boolean isUnaryOperator() {
        return OperatorOption.NOT.equals(this.operator);
    }
}
