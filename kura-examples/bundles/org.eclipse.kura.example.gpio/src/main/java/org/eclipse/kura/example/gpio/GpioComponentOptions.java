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
package org.eclipse.kura.example.gpio;

final class GpioComponentOptions {

    protected static final String INPUT_READ_MODE_PIN_STATUS_LISTENER = "PIN_STATUS_LISTENER";
    protected static final String INPUT_READ_MODE_POLLING = "POLLING";

    private String gpioServicePid;
    private String inputReadMode;
    private String[] pins;
    private int[] directions;
    private int[] modes;
    private int[] triggers;

    public GpioComponentOptions(GpioComponentOCD ocd) {

        this.gpioServicePid = ocd.gpio_service_pid();
        this.inputReadMode = ocd.gpio_input_read_mode();
        this.pins = ocd.gpio_pins();
        this.directions = ocd.gpio_directions();
        this.modes = ocd.gpio_modes();
        this.triggers = ocd.gpio_triggers();
    }

    public String getGpioServicePid() {
        return gpioServicePid;
    }

    public String getInputReadMode() {
        return inputReadMode;
    }

    public String[] getPins() {
        return pins;
    }

    public int[] getDirections() {
        return directions;
    }

    public int[] getModes() {
        return modes;
    }

    public int[] getTriggers() {
        return triggers;
    }

}
