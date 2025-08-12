/*******************************************************************************
 * Copyright (c) 2011, 2025 Eurotech and/or its affiliates and others
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
package org.eclipse.kura.raspberrypi.sensehat.ledmatrix;

public class Colors {

    private Colors() {
    }

    public static final short[] RED = { 0, 0, 255 };
    public static final short[] ORANGE = { 0, 127, 255 };
    public static final short[] YELLOW = { 0, 255, 255 };
    public static final short[] GREEN = { 0, 255, 0 };
    public static final short[] BLUE = { 255, 0, 0 };
    public static final short[] PURPLE = { 130, 0, 75 };
    public static final short[] VIOLET = { 255, 0, 159 };
    public static final short[] WHITE = { 255, 255, 255 };
    public static final short[] BLACK = { 0, 0, 0 };

    public static short[] fromColorString(String color) {
        return switch (color) {
            case "RED" -> RED;
            case "ORANGE" -> ORANGE;
            case "YELLOW" -> YELLOW;
            case "GREEN" -> GREEN;
            case "BLUE" -> BLUE;
            case "PURPLE" -> PURPLE;
            case "VIOLET" -> VIOLET;
            case "WHITE" -> WHITE;
            case "BLACK" -> BLACK;
            default -> throw new IllegalArgumentException("Unknown color: " + color);
        };
    }

}
