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
package org.eclipse.kura.example.driver;

import org.eclipse.kura.example.driver.ExampleDriverChannelDescriptor.UnitMeasure;

public class ExampleDriverOptions {

    private final UnitMeasure inputUnitMeasure;

    public ExampleDriverOptions(ExampleDriverOCD properties) {
        this.inputUnitMeasure = UnitMeasure.valueOf(properties.input_unit_measure());
    }

    public UnitMeasure getInputUnitMeasure() {
        return this.inputUnitMeasure;
    }

}
