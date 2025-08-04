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

package org.eclipse.kura.example.testutil;

// Class to hold the operands and operation name
public class OperandTriple<A, B, C> {

    private A firstArgument;
    private B secondArgument;
    private C thirdArgument;

    public OperandTriple(A firstArgument, B secondArgument, C thirdArgument) {
        this.firstArgument = firstArgument;
        this.secondArgument = secondArgument;
        this.thirdArgument = thirdArgument;
    }

    public A getFirstArgument() {
        return firstArgument;
    }

    public B getSecondArgument() {
        return secondArgument;
    }

    public C getThirdArgument() {
        return thirdArgument;
    }
}