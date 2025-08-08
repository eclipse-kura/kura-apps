/*******************************************************************************
 * Copyright (c) 2016, 2020 Red Hat Inc and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *  Red Hat Inc
 *  Eurotech
 *******************************************************************************/
package org.eclipse.kura.example.camel.publisher;

import static java.lang.Math.round;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;

/**
 * An example publisher based on Apache Camel
 */
public class ExampleCamelPublisher
        extends AbstractSimplePeriodicPublisher<ExampleCamelPublisher.PublisherConfiguration> {

    /**
     * Our configuration
     */
    public static final class PublisherConfiguration {

        private final int ampInt;
        private final int offsetInt;
        private final int periodInt;

        private final double ampDouble;
        private final double offsetDouble;
        private final int periodDouble;

        private PublisherConfiguration(ExampleCamelPublisherOCD ocd) {

            this.ampInt = ocd.integer_amplitude();
            this.offsetInt = ocd.integer_offset();
            this.periodInt = ocd.period_for_integer_value();

            this.ampDouble = ocd.floating_point_amplitude();
            this.offsetDouble = ocd.floating_point_offset();
            this.periodDouble = ocd.period_for_floating_point_value();
        }

        public int getAmpInt() {
            return this.ampInt;
        }

        public int getOffsetInt() {
            return this.offsetInt;
        }

        public int getPeriodInt() {
            return this.periodInt;
        }

        public double getAmpDouble() {
            return this.ampDouble;
        }

        public double getOffsetDouble() {
            return this.offsetDouble;
        }

        public int getPeriodDouble() {
            return this.periodDouble;
        }

    }

    public ExampleCamelPublisher() {
        super("camel/example");
    }

    @Override
    protected PublisherConfiguration parseConfiguration(ExampleCamelPublisherOCD ocd) {
        return PublisherConfiguration.fromOcd(ocd);
    }

    @Override
    protected Map<String, Object> getPayload(final PublisherConfiguration configuration) {

        // new result object

        final Map<String, Object> result = new HashMap<>(2);

        // fill with sine curves

        result.put("intValue",
                round(makeSine(configuration.getAmpInt(), configuration.getOffsetInt(), configuration.getPeriodInt())));
        result.put("doubleValue", makeSine(configuration.getAmpDouble(), configuration.getOffsetDouble(),
                configuration.getPeriodDouble()));

        // return result

        return result;
    }

    private static double makeSine(final double amp, final double offset, final double period) {
        final double freq = 1.0 / period * Math.PI * 2.0;
        final double v = System.currentTimeMillis() / 1000.0;

        return Math.sin(freq * v) * amp + offset;
    }

    @Override
    protected RouteBuilder fromOcd(ExampleCamelPublisherOCD ocd) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromOcd'");
    }
}
