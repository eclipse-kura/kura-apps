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

package org.eclipse.kura.example.driver.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.eclipse.kura.example.driver.ExampleDriver;
import org.eclipse.kura.example.driver.ExampleDriverChannelDescriptor.UnitMeasure;
import org.eclipse.kura.example.driver.ExampleDriverOCD;
import org.junit.Test;
import org.mockito.Mockito;

public class ExampleDriverTest {

    ExampleDriver exampleDriver = new ExampleDriver();

    @Test
    public void shouldActivate() {
        givenExampleDriver();

        whenActivatingExampleComponent();

        thenExampleOptionIs(UnitMeasure.METER);
    }

    private void givenExampleDriver() {
        this.exampleDriver = new ExampleDriver();

    }

    private void whenActivatingExampleComponent() {
        ExampleDriverOCD ocd = Mockito.mock(ExampleDriverOCD.class);
        when(ocd.input_unit_measure()).thenReturn("METER");
        this.exampleDriver.activate(ocd);
    }

    private void thenExampleOptionIs(UnitMeasure examplePropertyValue) {
        assertEquals(examplePropertyValue, this.exampleDriver.getOptions().getInputUnitMeasure());
    }
}
