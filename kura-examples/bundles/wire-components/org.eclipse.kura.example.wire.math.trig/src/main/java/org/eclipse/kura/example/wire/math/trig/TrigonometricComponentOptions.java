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

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrigonometricComponentOptions {

    public enum TrigonometricFunction {
        SIN,
        COS,
        TAN,
        ASIN,
        ACOS,
        ATAN
    }

    private static final TrigonometricFunction TRIGONOMETRIC_FUNCTION_DEFAULT = TrigonometricFunction.SIN;

    private static final Logger logger = LoggerFactory.getLogger(TrigonometricComponentOptions.class);

    private final String parameterName;
    private final String resultName;
    private final TrigonometricFunction operatorOption;
    private final boolean emitReceivedProperties;
    private final Function<Double, Double> trigonometricFunction;

    public TrigonometricComponentOptions(TrigonometricComponentOCD ocd) {
        this.parameterName = ocd.parameter_name();
        this.resultName = ocd.result_name();
        this.operatorOption = getLogicalOperator(ocd.trigonometric_function());
        this.trigonometricFunction = getTrigonometricFunction(this.operatorOption);
        this.emitReceivedProperties = ocd.emit_received_properties();
    }

    private TrigonometricFunction getLogicalOperator(String op) {
        try {
            return TrigonometricFunction.valueOf(op);
        } catch (Exception e) {
            logger.warn("Unknown operator, falling back to default operator {}", TRIGONOMETRIC_FUNCTION_DEFAULT);
            return TRIGONOMETRIC_FUNCTION_DEFAULT;
        }
    }

    public String getParameterName() {
        return this.parameterName;
    }

    public String getResultName() {
        return this.resultName;
    }

    public TrigonometricFunction getTrigonometricOperation() {
        return this.operatorOption;
    }

    public boolean shouldEmitReceivedProperties() {
        return this.emitReceivedProperties;
    }

    public Function<Double, Double> getTrigonometricFunction() {
        return this.trigonometricFunction;
    }

    private Function<Double, Double> getTrigonometricFunction(TrigonometricFunction o) {
        switch (o) {
        case COS:
            return parameter -> Math.cos(parameter.doubleValue());
        case TAN:
            return parameter -> Math.tan(parameter.doubleValue());
        case ASIN:
            return parameter -> Math.asin(parameter.doubleValue());
        case ACOS:
            return parameter -> Math.acos(parameter.doubleValue());
        case ATAN:
            return parameter -> Math.atan(parameter.doubleValue());
        case SIN:
        default:
            return parameter -> Math.sin(parameter.doubleValue());
        }
    }
}
