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
 ******************************************************************************/

package org.eclipse.kura.example.wire.logic.multiport.provider.test;

// Class to hold the operands and operation name
public class OperandTriple<A, B, C> {
    private A operationName;
    private B firstOperand;
    private C secondOperand;

    public OperandTriple(A operationName, B firstOperand, C secondOperand) {
        this.operationName = operationName;
        this.firstOperand = firstOperand;
        this.secondOperand = secondOperand;
    }

    public A getOperationName() {
        return operationName;
    }

    public B getFirstOperand() {
        return firstOperand;
    }

    public C getSecondOperand() {
        return secondOperand;
    }
}