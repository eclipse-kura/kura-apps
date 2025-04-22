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

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

// allow using _ in method names, it is needed for having ids containing '.'
@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition(id = "org.eclipse.kura.example.driver.ExampleDriver", //
        name = "ExampleDriver", //
        description = "An example driver component implementation." //
)
public interface ExampleDriverOCD {

    @AttributeDefinition(name = "Input Unit Measure", //
            defaultValue = "meter", //
            description = "Select the unit measure of the input data. During reading operation the ChannelRecord inputData setting is considered as input; during writing operation, the data set in the writing channel record is considered as input", //
            required = true, //
            cardinality = 0, //
            options = { //
                    @Option(label = "Meter", value = "meter"), //
                    @Option(label = "Nautic Mile", value = "nautic_mile"), //
                    @Option(label = "Yard", value = "yard"), //
                    @Option(label = "Inch", value = "inch") //
            } //
    )
    public String[] input_unit_measure();

}