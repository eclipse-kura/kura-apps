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

package org.eclipse.kura.example.wire.math.singleport;

public class MathSingleportProviderOptions {

    private final String operandName;
    private final String resultName;
    private final Integer windowSize;
    private final Boolean emitReceivedProperties;

    public MathSingleportProviderOptions(String operand, String result, Integer windSize, Boolean emitPorts) {
        this.operandName = operand;
        this.resultName = result;
        this.windowSize = windSize;
        this.emitReceivedProperties = emitPorts;
    }

    public String getOperandName() {
        return this.operandName;
    }

    public String getResultName() {
        return this.resultName;
    }

    public Integer getWindowSize() {
        return this.windowSize;
    }

    public Boolean shouldEmitReceivedProperties() {
        return this.emitReceivedProperties;
    }
}
