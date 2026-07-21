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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.junit.Test;

public class ContainerIdentityTokenExampleTest {

    private static final String TOKEN_FILE_ENV = "KURA_TOKEN_FILE";
    private static final String FALLBACK_PATH = "/run/secrets/kura-token";

    private final ContainerIdentityTokenExample example = new ContainerIdentityTokenExample();

    private final Map<String, String> environment = new HashMap<>();

    private String resolvedPath;
    private Optional<char[]> readToken;

    @Test
    public void shouldResolveTokenPathFromEnvironmentVariable() {
        givenEnvironmentVariable(TOKEN_FILE_ENV, "/custom/path/kura-token");

        whenTokenPathIsResolved(TOKEN_FILE_ENV, FALLBACK_PATH);

        thenResolvedPathIs("/custom/path/kura-token");
    }

    @Test
    public void shouldFallBackToConfiguredPathWhenEnvironmentVariableIsMissing() {
        // no environment variable set

        whenTokenPathIsResolved(TOKEN_FILE_ENV, FALLBACK_PATH);

        thenResolvedPathIs(FALLBACK_PATH);
    }

    @Test
    public void shouldFallBackToConfiguredPathWhenEnvironmentVariableIsBlank() {
        givenEnvironmentVariable(TOKEN_FILE_ENV, "   ");

        whenTokenPathIsResolved(TOKEN_FILE_ENV, FALLBACK_PATH);

        thenResolvedPathIs(FALLBACK_PATH);
    }

    @Test
    public void shouldReadPasswordFromTokenFile() throws IOException {
        final Path tokenFile = givenTokenFileContaining("s3cr3t-p4ss");

        whenTokenIsRead(tokenFile);

        thenTokenIsPresent();
        thenTokenContentIs("s3cr3t-p4ss");
    }

    @Test
    public void shouldReadPasswordWithoutAppendingNewline() throws IOException {
        final Path tokenFile = givenTokenFileContaining("no-newline");

        whenTokenIsRead(tokenFile);

        thenTokenIsPresent();
        thenTokenContentIs("no-newline");
    }

    @Test
    public void shouldReturnEmptyWhenTokenFileIsMissing() {
        final Path tokenFile = Path.of(System.getProperty("java.io.tmpdir"), "kura-token-does-not-exist");

        whenTokenIsRead(tokenFile);

        thenTokenIsEmpty();
    }

    @Test
    public void shouldReturnEmptyWhenTokenFileIsEmpty() throws IOException {
        final Path tokenFile = givenTokenFileContaining("");

        whenTokenIsRead(tokenFile);

        thenTokenIsEmpty();
    }

    /*
     * Given
     */

    private void givenEnvironmentVariable(final String name, final String value) {
        this.environment.put(name, value);
    }

    private Path givenTokenFileContaining(final String content) throws IOException {
        final Path tokenFile = Files.createTempFile("kura-token", null);
        tokenFile.toFile().deleteOnExit();
        Files.write(tokenFile, content.getBytes(StandardCharsets.UTF_8));
        return tokenFile;
    }

    /*
     * When
     */

    private void whenTokenPathIsResolved(final String envVarName, final String fallbackPath) {
        final UnaryOperator<String> env = this.environment::get;
        this.resolvedPath = this.example.resolveTokenPath(envVarName, fallbackPath, env);
    }

    private void whenTokenIsRead(final Path tokenFile) {
        this.readToken = this.example.readToken(tokenFile);
    }

    /*
     * Then
     */

    private void thenResolvedPathIs(final String expected) {
        assertEquals(expected, this.resolvedPath);
    }

    private void thenTokenIsPresent() {
        assertTrue(this.readToken.isPresent());
    }

    private void thenTokenIsEmpty() {
        assertFalse(this.readToken.isPresent());
    }

    private void thenTokenContentIs(final String expected) {
        assertArrayEquals(expected.toCharArray(), this.readToken.get());
    }
}
