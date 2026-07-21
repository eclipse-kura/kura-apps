/*******************************************************************************
 * Copyright (c) 2026 Eurotech and/or its affiliates and others
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
package org.eclipse.kura.example.container.identity.token;

import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Container Identity Token Example", //
        description = "An example that reads the temporary identity credentials that Kura provides to a container: "
                + "the identity name from an environment variable and the password from the read-only token file "
                + "mounted from tmpfs.", //
        id = "org.eclipse.kura.example.container.identity.token.ContainerIdentityTokenExample" //
)
@ComponentPropertyType
public @interface ContainerIdentityTokenExampleOCD {

    @AttributeDefinition(name = "Identity Name Environment Variable", //
            description = "The name of the environment variable that holds the identity name provided by Kura.", //
            defaultValue = "KURA_IDENTITY_NAME")
    String identityNameEnv();

    @AttributeDefinition(name = "Token File Environment Variable", //
            description = "The name of the environment variable that holds the path of the read-only token file "
                    + "provided by Kura.", //
            defaultValue = "KURA_TOKEN_FILE")
    String tokenFileEnv();

    @AttributeDefinition(name = "Token File Path Fallback", //
            description = "The token file path to use when the token file environment variable is not set. "
                    + "Useful for local testing outside of a container.", //
            defaultValue = "/run/secrets/kura-token", //
            required = false)
    String tokenFilePathFallback();
}
