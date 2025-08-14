/*******************************************************************************
 * Copyright (c) 2017, 2025 Red Hat Inc and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *  Red Hat Inc
 *******************************************************************************/
package org.eclipse.kura.example.camel.aggregation;

import java.security.SecureRandom;

import org.apache.camel.Exchange;
import org.apache.camel.support.ExpressionAdapter;

final class RandomTemperatureGenerator extends ExpressionAdapter {

    private int minimum;
    private int maximum;

    private final SecureRandom random = new SecureRandom();

    public RandomTemperatureGenerator(final int minimum, final int maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override
    public Object evaluate(final Exchange exchange) {
        return this.random.nextInt(this.maximum - this.minimum) + this.minimum;
    }

    public void setMinimum(final int minimum) {
        this.minimum = minimum;
    }

    public void setMaximum(final int maximum) {
        this.maximum = maximum;
    }

}
