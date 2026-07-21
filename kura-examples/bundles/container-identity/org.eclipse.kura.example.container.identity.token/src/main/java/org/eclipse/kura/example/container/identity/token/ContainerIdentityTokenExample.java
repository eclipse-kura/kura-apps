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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference example showing how a containerized application reads the temporary identity credentials that Kura
 * provides to a container it starts with identity integration enabled.
 * <p>
 * Kura passes the identity name in the {@code KURA_IDENTITY_NAME} environment variable and writes the identity
 * password to a read-only file mounted from tmpfs, whose in-container path is exported in the
 * {@code KURA_TOKEN_FILE} environment variable (defaulting to {@code /run/secrets/kura-token}). The password is
 * intentionally <em>not</em> passed through an environment variable, so it does not appear in {@code docker inspect}
 * or {@code /proc/<pid>/environ}.
 * <p>
 * This example only demonstrates how to <em>obtain</em> the credentials securely and never logs the password itself.
 * The subsequent step - performing an authenticated call to Kura's REST APIs - is intentionally omitted; see the
 * "Container Identity Credentials Migration Guide" in the Kura documentation for full request examples.
 * <p>
 * Note: like the other {@code kura-examples} projects this is packaged as an OSGi bundle so that it builds within the
 * examples reactor. The {@code KURA_TOKEN_FILE} environment variable only exists inside the container launched by
 * Kura, so the code below is meant as a reference for container-side applications rather than for a bundle running
 * inside the Kura framework itself.
 */
@Component(immediate = true, //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        service = ConfigurableComponent.class, //
        name = "org.eclipse.kura.example.container.identity.token.ContainerIdentityTokenExample" //
)
@Designate(ocd = ContainerIdentityTokenExampleOCD.class, factory = false)
public class ContainerIdentityTokenExample implements ConfigurableComponent {

    private static final Logger logger = LoggerFactory.getLogger(ContainerIdentityTokenExample.class);

    @Activate
    public void activate(final ContainerIdentityTokenExampleOCD ocd) {
        logger.info("Activating ContainerIdentityTokenExample...");
        readCredentials(ocd);
        logger.info("Activating ContainerIdentityTokenExample... Done.");
    }

    @Modified
    public void updated(final ContainerIdentityTokenExampleOCD ocd) {
        logger.info("Updating ContainerIdentityTokenExample...");
        readCredentials(ocd);
        logger.info("Updating ContainerIdentityTokenExample... Done.");
    }

    @Deactivate
    public void deactivate() {
        logger.info("Deactivating ContainerIdentityTokenExample...");
        logger.info("Deactivating ContainerIdentityTokenExample... Done.");
    }

    private void readCredentials(final ContainerIdentityTokenExampleOCD ocd) {
        final String identityName = System.getenv(ocd.identityNameEnv());
        if (identityName == null || identityName.isEmpty()) {
            logger.warn("Environment variable {} is not set: no identity provided by Kura. "
                    + "Is identity integration enabled for this container?", ocd.identityNameEnv());
            return;
        }

        final String tokenFilePath = resolveTokenPath(ocd.tokenFileEnv(), ocd.tokenFilePathFallback(), System::getenv);

        final Optional<char[]> token = readToken(Path.of(tokenFilePath));
        if (!token.isPresent()) {
            logger.error("Could not read a valid identity token from {}. "
                    + "The container startup should not have proceeded without it.", tokenFilePath);
            return;
        }

        final char[] password = token.get();
        try {
            // The password is now available in the 'password' array. A real application would use it, together with
            // 'identityName', to perform Basic-authenticated calls to Kura's REST APIs (KURA_REST_BASE_URL). That step
            // is intentionally omitted from this example; see the Container Identity Credentials Migration Guide.
            logger.info("Obtained credentials for identity '{}' from token file {} ({} chars). "
                    + "REST invocation is intentionally omitted from this example.", identityName, tokenFilePath,
                    password.length);
        } finally {
            // Clear the password from memory once it is no longer needed.
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Resolves the token file path, preferring the given environment variable and falling back to the configured path
     * when the variable is not set. Package-private for testability.
     */
    String resolveTokenPath(final String tokenFileEnvName, final String fallbackPath, final UnaryOperator<String> env) {
        final String fromEnv = env.apply(tokenFileEnvName);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return fallbackPath;
    }

    /**
     * Reads the identity password from the given token file. Returns an empty {@link Optional} when the file is
     * missing, empty or cannot be read. The file is expected to contain only the password, with no trailing newline.
     * Package-private for testability.
     */
    Optional<char[]> readToken(final Path tokenFile) {
        try {
            final byte[] bytes = Files.readAllBytes(tokenFile);
            if (bytes.length == 0) {
                logger.error("Token file {} is empty.", tokenFile);
                return Optional.empty();
            }
            return Optional.of(new String(bytes, StandardCharsets.UTF_8).toCharArray());
        } catch (final IOException e) {
            logger.error("Failed to read token file {}", tokenFile, e);
            return Optional.empty();
        }
    }
}
