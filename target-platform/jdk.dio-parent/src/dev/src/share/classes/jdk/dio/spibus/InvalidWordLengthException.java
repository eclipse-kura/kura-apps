/*
 * Copyright (c) 2013, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.dio.spibus;

/**
 * Thrown by an instance of {@link SPIDevice} in case of mismatch between the length of data to be exchanged and the
 * slave's word length as indicated by {@link SPIDevice#getWordLength SPIDevice.getWordLength}.
 *
 * @since 1.0
 */
@SuppressWarnings("serial")
public class InvalidWordLengthException extends RuntimeException {

    /**
     * Constructs a new {@code InvalidWordLengthException} with {@code null} as its detailed reason message.
     */
    public InvalidWordLengthException() {
        super();
    }

    /**
     * Constructs a new {@code InvalidWordLengthException} with the specified detail message. The error message string
     * {@code message} can later be retrieved by the {@link Throwable#getMessage() getMessage} method.
     *
     * @param message
     *            the detailed reason of the exception
     */
    public InvalidWordLengthException(String message) {
        super(message);
    }
}
