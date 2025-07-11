/*******************************************************************************
 * Copyright (c) 2024 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Eurotech
 ******************************************************************************/
package org.eclipse.kura.example.container.signature.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.configuration.Password;
import org.eclipse.kura.container.orchestration.ImageInstanceDescriptor;
import org.eclipse.kura.container.orchestration.PasswordRegistryCredentials;
import org.eclipse.kura.container.orchestration.RegistryCredentials;
import org.eclipse.kura.container.signature.ValidationResult;
import org.junit.Test;
import org.mockito.Mockito;

public class DummyContainerSignatureValidationServiceTest {

    private DummyContainerSignatureValidationService containerSignatureValidationService = new DummyContainerSignatureValidationService();
    private DummyContainerSignatureValidationServiceOCD ocd = Mockito
            .mock(DummyContainerSignatureValidationServiceOCD.class);

    private static final String IMAGE_ID = "imageId";
    private static final String TRUST_ANCHOR = "trustAnchor";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private static final ValidationResult FAILED_VALIDATION = new ValidationResult();

    private ValidationResult validationResult;
    private Exception occurredException;
    private ImageInstanceDescriptor imageDescriptor;

    @Test
    public void updatedWorksWithEmptyStringConfiguration() {
        givenOcdWithProperty("");

        whenUpdatedIsCalledWith(this.ocd);

        thenNoExceptionOccurred();
        thenConfiguredValidationResultsSizeIs(0);
    }

    @Test
    public void updatedWorksWithSingleStringConfiguration() {
        givenOcdWithProperty("alpine:latest@sha256:1234567890");

        whenUpdatedIsCalledWith(this.ocd);

        thenNoExceptionOccurred();
        thenConfiguredValidationResultsSizeIs(1);
        thenConfiguredValidationResultsContains("alpine", "latest", "sha256:1234567890");
    }

    @Test
    public void updatedWorksWithMultipleStringConfiguration() {
        givenOcdWithProperty("""
                alpine:latest@sha256:1234567890
                alpine:develop@sha256:1234567891
                ubuntu:latest@sha512:12345678911234567891
                """);

        whenUpdatedIsCalledWith(this.ocd);

        thenNoExceptionOccurred();
        thenConfiguredValidationResultsSizeIs(3);
        thenConfiguredValidationResultsContains("alpine", "latest", "sha256:1234567890");
        thenConfiguredValidationResultsContains("alpine", "develop", "sha256:1234567891");
        thenConfiguredValidationResultsContains("ubuntu", "latest", "sha512:12345678911234567891");
    }

    @Test
    public void updatedThrowsWithWrongFormatString() {
        givenOcdWithProperty("yalpine:latest:sha256:1234567890");

        whenUpdatedIsCalledWith(this.ocd);
        thenExceptionOccurred(IllegalArgumentException.class);
    }

    @Test
    public void verifyReturnsFailureWithEmptyConfiguration() {
        givenContainerSignatureValidationServiceWith(this.ocd);

        whenVerifyIsCalledWith("alpine", "latest", TRUST_ANCHOR, false);

        thenNoExceptionOccurred();
        thenVerificationResultIs(FAILED_VALIDATION);
    }

    @Test
    public void verifyReturnsFailureWithSingleEntryInConfiguration() {
        givenOcdWithProperty("alpine:latest@sha256:1234567890");
        givenContainerSignatureValidationServiceWith(this.ocd);

        whenVerifyIsCalledWith("alpine", "develop", TRUST_ANCHOR, false);

        thenNoExceptionOccurred();
        thenVerificationResultIs(FAILED_VALIDATION);
    }

    @Test
    public void verifyReturnsSuccessWithSingleEntryInConfiguration() {
        givenOcdWithProperty("alpine:latest@sha256:1234567890");
        givenContainerSignatureValidationServiceWith(this.ocd);

        whenVerifyIsCalledWith("alpine", "latest", TRUST_ANCHOR, false);

        thenNoExceptionOccurred();
        thenVerificationResultIs(new ValidationResult(true, "sha256:1234567890"));
    }

    @Test
    public void verifyWithAuthReturnsFailureWithSingleEntryInConfiguration() {
        givenOcdWithProperty("alpine:latest@sha256:1234567890");
        givenContainerSignatureValidationServiceWith(this.ocd);

        whenVerifyWithAuthIsCalledWith("alpine", "develop", TRUST_ANCHOR, false,
                new PasswordRegistryCredentials(Optional.empty(), USERNAME, new Password(PASSWORD)));

        thenNoExceptionOccurred();
        thenVerificationResultIs(FAILED_VALIDATION);
    }

    @Test
    public void verifyWithAuthReturnsSuccessWithSingleEntryInConfiguration() {
        givenOcdWithProperty("alpine:latest@sha256:1234567890");
        givenContainerSignatureValidationServiceWith(this.ocd);

        whenVerifyWithAuthIsCalledWith("alpine", "latest", TRUST_ANCHOR, false,
                new PasswordRegistryCredentials(Optional.empty(), USERNAME, new Password(PASSWORD)));

        thenNoExceptionOccurred();
        thenVerificationResultIs(new ValidationResult(true, "sha256:1234567890"));
    }

    @Test
    public void verifyWithImageReturnsFailureWithSingleEntryInConfiguration() {
        givenOcdWithProperty("alpine:latest@sha256:1234567890");
        givenImageInstanceDescriptorWith("alpine", "develop", IMAGE_ID);
        givenContainerSignatureValidationServiceWith(this.ocd);

        whenVerifyImageInstanceDescriptorIsCalledWith(this.imageDescriptor, TRUST_ANCHOR, false);

        thenNoExceptionOccurred();
        thenVerificationResultIs(FAILED_VALIDATION);
    }

    @Test
    public void verifyWithImageReturnsSuccessWithSingleEntryInConfiguration() {
        givenOcdWithProperty("alpine:latest@sha256:1234567890");
        givenImageInstanceDescriptorWith("alpine", "latest", IMAGE_ID);
        givenContainerSignatureValidationServiceWith(this.ocd);

        whenVerifyImageInstanceDescriptorIsCalledWith(this.imageDescriptor, TRUST_ANCHOR, false);

        thenNoExceptionOccurred();
        thenVerificationResultIs(new ValidationResult(true, "sha256:1234567890"));
    }

    @Test
    public void verifyWithImageWithAuthReturnsFailureWithSingleEntryInConfiguration() {
        givenOcdWithProperty("alpine:latest@sha256:1234567890");
        givenImageInstanceDescriptorWith("alpine", "develop", IMAGE_ID);
        givenContainerSignatureValidationServiceWith(this.ocd);

        whenVerifyImageInstanceDescriptorWithAuthIsCalledWith(this.imageDescriptor, TRUST_ANCHOR, false,
                new PasswordRegistryCredentials(Optional.empty(), USERNAME, new Password(PASSWORD)));

        thenNoExceptionOccurred();
        thenVerificationResultIs(FAILED_VALIDATION);
    }

    @Test
    public void verifyWithImageWithAuthReturnsSuccessWithSingleEntryInConfiguration() {
        givenOcdWithProperty("alpine:latest@sha256:1234567890");
        givenImageInstanceDescriptorWith("alpine", "latest", IMAGE_ID);
        givenContainerSignatureValidationServiceWith(this.ocd);

        whenVerifyImageInstanceDescriptorWithAuthIsCalledWith(this.imageDescriptor, TRUST_ANCHOR, false,
                new PasswordRegistryCredentials(Optional.empty(), USERNAME, new Password(PASSWORD)));

        thenNoExceptionOccurred();
        thenVerificationResultIs(new ValidationResult(true, "sha256:1234567890"));
    }

    /*
     * GIVEN
     */
    private void givenContainerSignatureValidationServiceWith(
            DummyContainerSignatureValidationServiceOCD configuration) {
        this.containerSignatureValidationService.activate(configuration);
    }

    private void givenOcdWithProperty(String value) {
        Mockito.when(this.ocd.set_signature_validation_outcome()).thenReturn(value);
    }

    private void givenImageInstanceDescriptorWith(String imageName, String imageTag, String imageId) {
        this.imageDescriptor = ImageInstanceDescriptor.builder().setImageName(imageName).setImageTag(imageTag)
                .setImageId(imageId).setImageLabels(new HashMap<>()).build();
    }

    /*
     * WHEN
     */
    private void whenUpdatedIsCalledWith(DummyContainerSignatureValidationServiceOCD props) {
        try {
            this.containerSignatureValidationService.updated(props);
        } catch (Exception e) {
            this.occurredException = e;
        }
    }

    private void whenVerifyIsCalledWith(String imageName, String imageTag, String trustAnchor, boolean isVerify) {
        try {
            this.validationResult = this.containerSignatureValidationService.verify(imageName, imageTag, trustAnchor,
                    isVerify);
        } catch (KuraException e) {
            this.occurredException = e;
        }
    }

    private void whenVerifyWithAuthIsCalledWith(String imageName, String imageTag, String trustAnchor, boolean isVerify,
            RegistryCredentials credentials) {
        try {
            this.validationResult = this.containerSignatureValidationService.verify(imageName, imageTag, trustAnchor,
                    isVerify, credentials);
        } catch (KuraException e) {
            this.occurredException = e;
        }
    }

    private void whenVerifyImageInstanceDescriptorIsCalledWith(ImageInstanceDescriptor descriptor, String trustAnchor,
            boolean isVerify) {
        try {
            this.validationResult = this.containerSignatureValidationService.verify(descriptor, trustAnchor, isVerify);
        } catch (KuraException e) {
            this.occurredException = e;
        }
    }

    private void whenVerifyImageInstanceDescriptorWithAuthIsCalledWith(ImageInstanceDescriptor descriptor,
            String trustAnchor, boolean isVerify, RegistryCredentials credentials) {
        try {
            this.validationResult = this.containerSignatureValidationService.verify(descriptor, trustAnchor, isVerify,
                    credentials);
        } catch (KuraException e) {
            this.occurredException = e;
        }
    }

    /*
     * THEN
     */
    private void thenConfiguredValidationResultsContains(String imageName, String imageTag, String expectedDigest) {
        String digest = this.containerSignatureValidationService.getConfiguredValidationResultsFor(imageName, imageTag);
        assertTrue(Objects.nonNull(digest));
        assertEquals(expectedDigest, digest);
    }

    private void thenConfiguredValidationResultsSizeIs(int expectedSize) {
        assertEquals(expectedSize, this.containerSignatureValidationService.getConfiguredValidationResultsSize());
    }

    private void thenVerificationResultIs(ValidationResult expectedResult) {
        assertEquals(expectedResult, this.validationResult);
    }

    private void thenNoExceptionOccurred() {
        String errorMessage = "Empty message";
        if (Objects.nonNull(this.occurredException)) {
            StringWriter sw = new StringWriter();
            this.occurredException.printStackTrace(new PrintWriter(sw));

            errorMessage = String.format("No exception expected, \"%s\" found. Caused by: %s",
                    this.occurredException.getClass().getName(), sw.toString());
        }

        assertNull(errorMessage, this.occurredException);
    }

    private <E extends Exception> void thenExceptionOccurred(Class<E> expectedException) {
        assertNotNull(this.occurredException);
        assertEquals(expectedException.getName(), this.occurredException.getClass().getName());
    }
}
