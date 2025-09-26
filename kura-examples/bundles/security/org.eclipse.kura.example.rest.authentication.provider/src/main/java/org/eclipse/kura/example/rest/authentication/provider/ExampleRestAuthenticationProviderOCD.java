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

package org.eclipse.kura.example.rest.authentication.provider;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition( //
        id = "org.eclipse.kura.example.rest.authentication.provider.ExampleRestAuthenticationProvider", //
        name = "Example Rest Authentication Provider", //
        description = "This component provides an example implementation of a REST authentication provider." //

)
@ComponentPropertyType
public @interface ExampleRestAuthenticationProviderOCD {
}