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
package org.eclipse.kura.example.identity.configuration.extension;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Example Identity Configuration Extension", //
                description = "An example identity configuration extension", //
                id = "org.eclipse.kura.example.identity.configuration.extension.ExampleIdentityConfigurationExtension" //
)
@ComponentPropertyType
public @interface ExampleIdentityConfigurationExtensionOCD {

        // No options needed for this example
}
