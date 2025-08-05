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
 ******************************************************************************/

package org.eclipse.kura.example.wire.logic.multiport.provider.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.configuration.ComponentConfiguration;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.example.testutil.OperandTriple;
import org.eclipse.kura.type.TypedValue;
import org.eclipse.kura.type.TypedValues;
import org.eclipse.kura.util.wire.test.GraphBuilder;
import org.eclipse.kura.util.wire.test.TestEmitterReceiver;
import org.eclipse.kura.util.wire.test.WireTestUtil;
import org.eclipse.kura.wire.WireEnvelope;
import org.eclipse.kura.wire.WireRecord;
import org.eclipse.kura.wire.graph.WireGraphConfiguration;
import org.eclipse.kura.wire.graph.WireGraphService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogicalComponentItTest {

    private static final Logger logger = LoggerFactory.getLogger(LogicalComponentItTest.class);

    private static final String FACTORY_PID = "org.eclipse.kura.example.wire.logic.multiport.provider.LogicalComponent";
    private static final String TEST_FIRST_EMITTER_PID = "test.first.emitter.pid";
    private static final String TEST_SECOND_EMITTER_PID = "test.second.emitter.pid";
    private static final String TEST_RECEIVER_PID = "test.receiver.pid";

    // in ports
    private static final int IN0_PORT = 0;
    private static final int IN1_PORT = 1;

    // out ports
    private static final int OUT_PORT = 0;

    private static TestEmitterReceiver in1emitter;
    private static TestEmitterReceiver outReceiver;
    private static TestEmitterReceiver in0emitter;

    // configuration properties of component under test
    private static final String FIRST_OPERAND_NAME_PROP_NAME = "operand.name.1";
    private static final String SECOND_OPERAND_NAME_PROP_NAME = "operand.name.2";
    private static final String BARRIER_MODALITY_PROPERTY_KEY = "barrier";
    private static final String BOOLEAN_OPERATION = "logical.operator";

    private static final String OPERAND_NAME_DEFAULT = "operand";
    private static final String RESULT_NAME_DEFAULT = "result";
    private static final boolean BARRIER_MODALITY_PROPERTY_DEFAULT = false;
    private static final String BOOLEAN_OPERATION_DEFAULT = "AND";

    private static Map<String, TypedValue<?>> mapWithTrue = new HashMap<>();
    private static Map<String, TypedValue<?>> mapWithFalse = new HashMap<>();

    private static List<OperandTriple<String, Boolean, Boolean>> operandsCombination = new ArrayList<>();
    private Map<String, Boolean> operationResultsMap = new HashMap<>();

    private static ConfigurationService configurationService;
    private static WireGraphService wireGraphService;

    private ConfigurableComponent configurableComponent;

    private final GraphBuilder builder = new GraphBuilder();
    private WireGraphConfiguration wireGraphConfiguration;
    private final BundleContext bundleContext = FrameworkUtil.getBundle(LogicalComponentItTest.class)
            .getBundleContext();

    String activeWirePid;

    @Test
    public void shouldCreateWireComponent() {
        givenWireComponent();

        thenWireComponentIsNotNull();
    }

    @Test
    public void shouldExistsWireComponent() throws KuraException {
        givenWireGraphConfiguration();

        thenWireGraphExistsWithPid(this.activeWirePid);

    }

    @Test
    public void shouldWireComponentHasDefaultProperties() throws KuraException {
        givenWireGraphConfiguration();

        thenWireGraphConfigurationIsDefaultOne();

    }

    @Test
    public void shouldPerformAndOperationCorrectly() throws Exception {
        givenLogicalOperation("AND");

        givenBooleanOperationWithName("tt");
        givenBooleanOperationWithName("tf");
        givenBooleanOperationWithName("ft");
        givenBooleanOperationWithName("ff");

        thenResultMapContainsExpectedValues("tt", true);
        thenResultMapContainsExpectedValues("tf", false);
        thenResultMapContainsExpectedValues("ft", false);
        thenResultMapContainsExpectedValues("ff", false);
    }

    @Test
    public void shouldPerformXorOperationCorrectly() throws Exception {
        givenLogicalOperation("XOR");

        givenBooleanOperationWithName("tt");
        givenBooleanOperationWithName("tf");
        givenBooleanOperationWithName("ft");
        givenBooleanOperationWithName("ff");

        thenResultMapContainsExpectedValues("tt", false);
        thenResultMapContainsExpectedValues("tf", true);
        thenResultMapContainsExpectedValues("ft", true);
        thenResultMapContainsExpectedValues("ff", false);
    }

    @Test
    public void shouldPerformOrOperationCorrectly() throws Exception {
        givenLogicalOperation("OR");

        givenBooleanOperationWithName("tt");
        givenBooleanOperationWithName("tf");
        givenBooleanOperationWithName("ft");
        givenBooleanOperationWithName("ff");

        thenResultMapContainsExpectedValues("tt", true);
        thenResultMapContainsExpectedValues("tf", true);
        thenResultMapContainsExpectedValues("ft", true);
        thenResultMapContainsExpectedValues("ff", false);
    }

    @Test
    public void shouldPerformNorOperationCorrectly() throws Exception {
        givenLogicalOperation("NOR");

        givenBooleanOperationWithName("tt");
        givenBooleanOperationWithName("tf");
        givenBooleanOperationWithName("ft");
        givenBooleanOperationWithName("ff");

        thenResultMapContainsExpectedValues("tt", false);
        thenResultMapContainsExpectedValues("tf", false);
        thenResultMapContainsExpectedValues("ft", false);
        thenResultMapContainsExpectedValues("ff", true);
    }

    @Test
    public void shouldPerformNandOperationCorrectly() throws Exception {

        givenLogicalOperation("NAND");

        givenBooleanOperationWithName("tt");
        givenBooleanOperationWithName("tf");
        givenBooleanOperationWithName("ft");
        givenBooleanOperationWithName("ff");

        thenResultMapContainsExpectedValues("tt", false);
        thenResultMapContainsExpectedValues("tf", true);
        thenResultMapContainsExpectedValues("ft", true);
        thenResultMapContainsExpectedValues("ff", true);

    }

    @Test
    public void testNotOperation() throws Exception {
        givenLogicalOperation("NOT");

        thenNotOperationIsCorrect();
    }

    @Test
    public void testIllegalOperation() throws Exception {
        givenLogicalOperation("ILLEGAL");

        givenBooleanOperationWithName("tt");
        givenBooleanOperationWithName("tf");
        givenBooleanOperationWithName("ft");
        givenBooleanOperationWithName("ff");

        thenResultMapContainsExpectedValues("tt", true);
        thenResultMapContainsExpectedValues("tf", false);
        thenResultMapContainsExpectedValues("ft", false);
        thenResultMapContainsExpectedValues("ff", false);
    }

    @BeforeClass
    public static void setupConfigurationService()
            throws InterruptedException, ExecutionException, TimeoutException, KuraException {
        try {
            configurationService = WireTestUtil.trackService(ConfigurationService.class, Optional.empty()).get(30,
                    TimeUnit.SECONDS);
            wireGraphService = WireTestUtil.trackService(WireGraphService.class, Optional.empty()).get(30,
                    TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup WireGraphService", e);
        }

        operandsCombination.add(new OperandTriple<>("tt", true, true));
        operandsCombination.add(new OperandTriple<>("tf", true, false));
        operandsCombination.add(new OperandTriple<>("ft", false, true));
        operandsCombination.add(new OperandTriple<>("ff", false, false));
    }

    @Before
    public void setUp() throws Exception {

        this.activeWirePid = "underTestPid";

        builder.addWireComponent(this.activeWirePid, FACTORY_PID, 2, 1); // 2 input ports, 1 output port
        builder.addTestEmitterReceiver(TEST_FIRST_EMITTER_PID);
        builder.addTestEmitterReceiver(TEST_SECOND_EMITTER_PID);
        builder.addTestEmitterReceiver(TEST_RECEIVER_PID);
        builder.addWire(TEST_FIRST_EMITTER_PID, 0, this.activeWirePid, IN0_PORT);
        builder.addWire(TEST_SECOND_EMITTER_PID, 0, this.activeWirePid, IN1_PORT);
        builder.addWire(this.activeWirePid, OUT_PORT, TEST_RECEIVER_PID, 0);

        try {
            builder.replaceExistingGraph(bundleContext, wireGraphService).get(30, TimeUnit.SECONDS);

            in1emitter = builder.getTrackedWireComponent(TEST_SECOND_EMITTER_PID);
            in0emitter = builder.getTrackedWireComponent(TEST_FIRST_EMITTER_PID);
            outReceiver = builder.getTrackedWireComponent(TEST_RECEIVER_PID);
        } catch (KuraException | ExecutionException e) {
            logger.error("Test error", e);
            throw e;
        }

        mapWithTrue.put(OPERAND_NAME_DEFAULT, TypedValues.newBooleanValue(true));
        mapWithFalse.put(OPERAND_NAME_DEFAULT, TypedValues.newBooleanValue(false));
    }

    @After
    public void tearDown() throws Exception {
        try {
            wireGraphService.delete();
        } catch (KuraException e) {
            logger.error("Test error", e);
            throw e;
        }
    }

    private void givenLogicalOperation(String operation)
            throws InterruptedException, ExecutionException, TimeoutException {

        Map<String, Object> props = new HashMap<>();
        props.put(BOOLEAN_OPERATION, operation);
        WireTestUtil.updateWireComponentConfiguration(configurationService, this.activeWirePid, props).get(30,
                TimeUnit.SECONDS);
    }

    private void givenWireComponent() {
        this.configurableComponent = builder.getTrackedWireComponent(this.activeWirePid);
    }

    private void givenWireGraphConfiguration() throws KuraException {
        this.wireGraphConfiguration = wireGraphService.get();
    }

    private void thenWireComponentIsNotNull() {
        Assert.assertNotNull("The Wire Component should not be null", this.configurableComponent);
    }

    private void thenWireGraphExistsWithPid(String expectedPid) {
        assertTrue(this.wireGraphConfiguration.getWireComponentConfigurations().stream()
                .anyMatch(wcc -> wcc.getConfiguration().getPid().equals(expectedPid)));
    }

    private void thenWireGraphConfigurationIsDefaultOne() {
        assertTrue(this.wireGraphConfiguration.getWireComponentConfigurations().stream()
                .anyMatch(wcc -> matchesDefaultConfiguration(wcc.getConfiguration())));
    }

    private void thenResultMapContainsExpectedValues(String key, Boolean expectedValue) {
        assertEquals(expectedValue, this.operationResultsMap.get(key));
    }

    private void thenNotOperationIsCorrect() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<WireEnvelope> out0Recfuture = outReceiver.nextEnvelope();
        in0emitter.emit(new WireRecord(mapWithTrue));
        WireRecord receivedRecord = out0Recfuture.get(1, TimeUnit.SECONDS).getRecords().get(0);
        assertFalse((boolean) receivedRecord.getProperties().get(RESULT_NAME_DEFAULT).getValue());
        out0Recfuture = outReceiver.nextEnvelope();
        in0emitter.emit(new WireRecord(mapWithFalse));
        receivedRecord = out0Recfuture.get(1, TimeUnit.SECONDS).getRecords().get(0);
        assertTrue((boolean) receivedRecord.getProperties().get(RESULT_NAME_DEFAULT).getValue());
    }

    private boolean matchesDefaultConfiguration(ComponentConfiguration cc) {
        if (cc.getPid().equals(this.activeWirePid)) {
            Map<String, Object> props = cc.getConfigurationProperties();
            return OPERAND_NAME_DEFAULT.equals(props.get(FIRST_OPERAND_NAME_PROP_NAME))
                    && OPERAND_NAME_DEFAULT.equals(props.get(SECOND_OPERAND_NAME_PROP_NAME))
                    && BARRIER_MODALITY_PROPERTY_DEFAULT == (boolean) props.get(BARRIER_MODALITY_PROPERTY_KEY)
                    && BOOLEAN_OPERATION_DEFAULT.equals(props.get(BOOLEAN_OPERATION));
        }
        return false;
    }

    private void givenBooleanOperationWithName(String operationName)
            throws InterruptedException, ExecutionException, TimeoutException {

        Optional<OperandTriple<String, Boolean, Boolean>> optTriple = operandsCombination.stream()
                .filter(operand -> operand.getFirstArgument().equals(operationName)).findFirst();

        if (optTriple.isPresent()) {
            OperandTriple<String, Boolean, Boolean> triple = optTriple.get();
            this.operationResultsMap.put(operationName,
                    performBooleanOperation(triple.getSecondArgument(), triple.getThirdArgument()));
        } else {
            throw new IllegalArgumentException("Operation name not found: " + operationName);
        }

    }

    private Boolean performBooleanOperation(boolean operator1, boolean operator2)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (operator1) {
            in0emitter.emit(new WireRecord(mapWithTrue));
        } else {
            in0emitter.emit(new WireRecord(mapWithFalse));
        }
        CompletableFuture<WireEnvelope> out0Recfuture = outReceiver.nextEnvelope();
        if (operator2) {
            in1emitter.emit(new WireRecord(mapWithTrue));
        } else {
            in1emitter.emit(new WireRecord(mapWithFalse));
        }
        WireRecord receivedRecord = out0Recfuture.get(1, TimeUnit.SECONDS).getRecords().get(0);
        return (boolean) receivedRecord.getProperties().get(RESULT_NAME_DEFAULT).getValue();
    }

}
