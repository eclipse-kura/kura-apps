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

import java.util.Map;

import org.eclipse.kura.example.driver.ExampleDriverChannelDescriptor.UnitMeasure;
import org.eclipse.kura.example.driver.utils.Property;

public class ExampleDriverOptions {

    private static final Property<String> FIRST_ADDEND = new Property<>("input.unit.measure", "meter");

    private final UnitMeasure inputUnitMeasure;

    public ExampleDriverOptions(final Map<String, Object> properties) {
        this.inputUnitMeasure = UnitMeasure.valueOf(FIRST_ADDEND.getOrDefault(properties));
    }

    public UnitMeasure getInputUnitMeasure() {
        return this.inputUnitMeasure;
    }

}
