/*******************************************************************************
 * Copyright (c) 2024 Eurotech and/or its affiliates. All rights reserved.
 *******************************************************************************/
package org.eclipse.kura.example.driver.utils;

import org.eclipse.kura.configuration.Password;
import org.eclipse.kura.core.configuration.metatype.Tad;
import org.eclipse.kura.core.configuration.metatype.Tscalar;

public class AdProperty<T> extends Property<T> {

    private final Tad ad;

    private AdProperty(final Tad tad, final T defaultValue) {
        super(tad.getId(), defaultValue);
        this.ad = tad;
    }

    public static <T> AdProperty<T> required(final String id, final String name, final String description,
            final T defaultValue) {

        final Tad tad = new Tad();
        tad.setId(id);
        tad.setName(name);
        tad.setCardinality(0);
        tad.setRequired(true);
        tad.setDefault(defaultValue.toString());
        tad.setType(fromType(defaultValue.getClass()));
        tad.setDescription(description);

        return new AdProperty<>(tad, defaultValue);
    }

    public static <T> AdProperty<T> optional(final String id, final String name, final String description,
            final T defaultValue) {

        final Tad tad = new Tad();
        tad.setId(id);
        tad.setName(name);
        tad.setCardinality(0);
        tad.setRequired(false);
        tad.setDefault(null);
        tad.setType(fromType(defaultValue.getClass()));
        tad.setDescription(description);

        return new AdProperty<>(tad, defaultValue);
    }

    private static Tscalar fromType(final Class<?> type) {
        if (type == Boolean.class) {
            return Tscalar.BOOLEAN;
        } else if (type == Byte.class) {
            return Tscalar.BYTE;
        } else if (type == Character.class) {
            return Tscalar.CHAR;
        } else if (type == Double.class) {
            return Tscalar.DOUBLE;
        } else if (type == Float.class) {
            return Tscalar.FLOAT;
        } else if (type == Integer.class) {
            return Tscalar.INTEGER;
        } else if (type == Long.class) {
            return Tscalar.LONG;
        } else if (type == Password.class) {
            return Tscalar.PASSWORD;
        } else if (type == Short.class) {
            return Tscalar.SHORT;
        } else if (type == String.class) {
            return Tscalar.STRING;
        }

        throw new IllegalArgumentException(type == null ? null : type.toString());
    }

    public Tad getAd() {
        return this.ad;
    }

}