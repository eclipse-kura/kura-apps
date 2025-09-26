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

package org.eclipse.kura.example.camel.publisher;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Icon;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Configurable Apache Camel based example publisher. It publishes to
 * 'camel/example'.
 */
@ObjectClassDefinition( //
                id = "org.eclipse.kura.example.camel.publisher.ExampleCamelPublisher", //
                name = "Camel example publisher", //
                description = "Configurable Apache Camel based example publisher. It publishes to 'camel/example'.", //
                icon = { //
                                @Icon(resource = "logos/logo.png", size = 32) //
                } //
)
public @interface ExampleCamelPublisherOCD {

        @AttributeDefinition(name = "Enable Service", //
                        description = "If the service is enabled it will publish data", //
                        cardinality = 1, //
                        required = true //
        )
        boolean enable_service() default true;

        @AttributeDefinition(name = "Cloud Service PID", //
                        description = "The service PID of the Cloud Service to use", //
                        cardinality = 1, //
                        required = false //
        )
        String cloud_service_pid() default "org.eclipse.kura.cloud.CloudService";

        @AttributeDefinition(name = "Integer Amplitude", //
                        description = "The amplitude of the integer value", //
                        cardinality = 0, //
                        required = true //
        )
        int integer_amplitude() default -20;

        @AttributeDefinition(name = "Integer Offset", //
                        description = "The offset of the integer value", //
                        cardinality = 0, //
                        required = true //
        )
        int integer_offset() default 20;

        @AttributeDefinition(name = "Period For Integer Value", //
                        description = "This value specifies the period time in seconds for the integer value", //
                        cardinality = 0, //
                        required = true //
        )
        int period_for_integer_value() default 60;

        @AttributeDefinition(name = "Floating Point Amplitude", //
                        description = "The amplitude of the floating point value", //
                        cardinality = 0, //
                        required = true //
        )
        double floating_point_amplitude() default -0.5;

        @AttributeDefinition(name = "Floating Point Offset", //
                        description = "The offset of the floating point value", //
                        cardinality = 0, //
                        required = true //
        )
        double floating_point_offset() default 0.5;

        @AttributeDefinition(name = "Period For Floating Point Value", //
                        description = "This value specifies the period time in seconds for the floating point value", //
                        cardinality = 0, //
                        required = true //
        )
        int period_for_floating_point_value() default 30;
}
