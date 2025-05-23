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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Alphabet {

    private static final Logger logger = LoggerFactory.getLogger(Alphabet.class);

    private static final String ALPABHET = " +-*/!\"#$><0123456789.=)(ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz?,;:|@%[&_']\\~";
    private static InputStream is;
    private static Map<String, short[][][]> letters;

    public Alphabet(URL url) {

        letters = new HashMap<>();
        short[][][] letter = new short[8][8][3];
        int c;
        try {
            is = url.openStream();
            for (int i = 0; i < 91; i++) {
                letter = new short[8][8][3];
                for (int y = 7; y >= 0; y--) {
                    for (int x = 0; x < 8; x++) {
                        if (y == 0 || y == 6 || y == 7) {
                            // Add whitespaces
                            letter[x][y][0] = 0;
                            letter[x][y][1] = 0;
                            letter[x][y][2] = 0;
                        } else {
                            c = is.read();
                            if (c == '\n') {
                                c = is.read();
                            }
                            if (c == 48) { // ASCII 48 -> 0 (inverted)
                                letter[x][y][0] = 1;
                                letter[x][y][1] = 1;
                                letter[x][y][2] = 1;
                            } else {
                                letter[x][y][0] = 0;
                                letter[x][y][1] = 0;
                                letter[x][y][2] = 0;
                            }
                        }
                    }
                }
                letters.put(Character.toString(ALPABHET.charAt(i)), rotate(letter));
            }
        } catch (IOException e) {
            logger.error("Error in opening Alphabet file.", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error("Error in closing stream.", e);
                }
            }
        }
    }

    public short[][][] getLetter(String letter) {

        return letters.get(letter);
    }

    public boolean isAvailable(String letter) {
        return ALPABHET.contains(letter);
    }

    private short[][][] rotate(short[][][] letter) {

        short[][][] rotatedLetter = new short[8][8][3];
        short[] pixel = new short[3];

        rotatedLetter = transpose(letter);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 8; y++) {
                pixel = rotatedLetter[x][y];
                rotatedLetter[x][y] = rotatedLetter[8 - 1 - x][y];
                rotatedLetter[8 - 1 - x][y] = pixel;
            }
        }

        return rotatedLetter;
    }

    private short[][][] transpose(short[][][] letter) {

        short[][][] transposedLetter = new short[8][8][3];

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                transposedLetter[x][y] = letter[y][x];
                transposedLetter[y][x] = letter[x][y];
            }
        }

        return transposedLetter;
    }
}
