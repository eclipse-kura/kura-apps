/*******************************************************************************
 * Copyright (c) 2020, 2025 Eurotech and/or its affiliates and others
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

package org.eclipse.kura.example.wire.math.trig.test;

import static org.junit.Assert.assertEquals;
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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class TrigonometricComponentTest {

    private static final Logger logger = LoggerFactory.getLogger(TrigonometricComponentTest.class);

    private static final String FACTORY_PID = "org.eclipse.kura.wire.TrigonometricFunctions";
    private static final String TEST_EMITTER_PID = "test.emitter.pid";
    private static final String TEST_RECEIVER_PID = "test.receiver.pid";

    // in ports
    private static final int IN_PORT = 0;

    // out ports
    private static final int OUT_PORT = 0;

    // configuration properties of component under test
    private static final String PARAMETER_NAME_PROP_NAME = "parameter.name";
    private static final String RESULT_NAME_PROP_NAME = "result.name";
    private static final String EMIT_RECEIVED_PROPERTIES = "emit.received.properties";
    private static final String TRIGONOMETRIC_OPERATION = "trigonometric.function";

    private static final String PARAMETER_NAME_DEFAULT = "parameter";
    private static final String RESULT_NAME_DEFAULT = "result";
    private static final boolean EMIT_RECEIVED_PROPERTIES_DEFAULT = false;
    private static final String TRIGONOMETRIC_OPERATION_DEFAULT = "SIN";

    private static WireGraphService wireGraphService;
    private static ConfigurationService configurationService;

    private static TestEmitterReceiver outReceiver;
    private static TestEmitterReceiver inEmitter;

    private final GraphBuilder builder = new GraphBuilder();
    private WireGraphConfiguration wireGraphConfiguration;
    private final BundleContext bundleContext = FrameworkUtil.getBundle(TrigonometricComponentTest.class)
            .getBundleContext();

    String activeWirePid;
    private static List<OperandTriple<String, String, TypedValue<Double>>> operandTriples = new ArrayList<>();
    private Map<String, Double> resultsMap = new HashMap<>();

    @BeforeClass
    public static void setupDependencies()
            throws InterruptedException, ExecutionException, TimeoutException, KuraException {
        try {
            configurationService = WireTestUtil.trackService(ConfigurationService.class, Optional.empty()).get(30,
                    TimeUnit.SECONDS);
            wireGraphService = WireTestUtil.trackService(WireGraphService.class, Optional.empty()).get(30,
                    TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup WireGraphService", e);
        }

        operandTriples.add(new OperandTriple<String, String, TypedValue<Double>>("zero", PARAMETER_NAME_DEFAULT,
                TypedValues.newDoubleValue(0)));
        operandTriples.add(new OperandTriple<String, String, TypedValue<Double>>("half", PARAMETER_NAME_DEFAULT,
                TypedValues.newDoubleValue(0.5)));
        operandTriples.add(new OperandTriple<String, String, TypedValue<Double>>("quarterPi", PARAMETER_NAME_DEFAULT,
                TypedValues.newDoubleValue(Math.PI / 4)));
        operandTriples.add(new OperandTriple<String, String, TypedValue<Double>>("one", PARAMETER_NAME_DEFAULT,
                TypedValues.newDoubleValue(1)));
        operandTriples.add(new OperandTriple<String, String, TypedValue<Double>>("halfPi", PARAMETER_NAME_DEFAULT,
                TypedValues.newDoubleValue(Math.PI / 2)));
        operandTriples.add(new OperandTriple<String, String, TypedValue<Double>>("pi", PARAMETER_NAME_DEFAULT,
                TypedValues.newDoubleValue(Math.PI)));
        operandTriples.add(new OperandTriple<String, String, TypedValue<Double>>("threeHalvesPi",
                PARAMETER_NAME_DEFAULT, TypedValues.newDoubleValue(Math.PI * 1.5)));
        operandTriples.add(new OperandTriple<String, String, TypedValue<Double>>("twoPi", PARAMETER_NAME_DEFAULT,
                TypedValues.newDoubleValue(Math.PI * 2)));
    }

    @Before
    public void setUp() throws KuraException, InterruptedException, TimeoutException, ExecutionException {

        this.activeWirePid = "componentUnderTest";

        builder.addWireComponent(this.activeWirePid,
                FACTORY_PID, 1, 1) //
                .addTestEmitterReceiver(TEST_EMITTER_PID) //
                .addTestEmitterReceiver(TEST_RECEIVER_PID) //
                .addWire(TEST_EMITTER_PID, 0, this.activeWirePid, IN_PORT) //
                .addWire(this.activeWirePid, OUT_PORT, TEST_RECEIVER_PID, 0);

        try {
            builder.replaceExistingGraph(bundleContext, wireGraphService).get(30, TimeUnit.SECONDS);

            inEmitter = builder.getTrackedWireComponent(TEST_EMITTER_PID);
            outReceiver = builder.getTrackedWireComponent(TEST_RECEIVER_PID);
        } catch (KuraException | ExecutionException e) {
            logger.error("Test error", e);
            throw e;
        }
    }

    @Test
    public void wireComponentExists() throws KuraException {

        givenWireGraphConfiguration();

        thenWireGraphExistsWithPid(this.activeWirePid);
    }

    @Test
    public void wireComponentHasDefaultProperties() throws KuraException {
        givenWireGraphConfiguration();

        thenWireGraphConfigurationIsDefaultOne();
    }

    @Test
    public void testSinOperation() throws Exception {
        givenTrigonometricOperation("SIN");

        whenTrigonometricOperationWithValue("zero");
        whenTrigonometricOperationWithValue("halfPi");
        whenTrigonometricOperationWithValue("pi");
        whenTrigonometricOperationWithValue("threeHalvesPi");

        thenResultsEqualsExpectedValue("zero", Math.sin(0));
        thenResultsEqualsExpectedValue("halfPi", Math.sin(Math.PI / 2));
        thenResultsEqualsExpectedValue("pi", Math.sin(Math.PI));
        thenResultsEqualsExpectedValue("threeHalvesPi", Math.sin(Math.PI * 1.5));

    }

    @Test
    public void testCosOperation() throws Exception {
        givenTrigonometricOperation("COS");

        whenTrigonometricOperationWithValue("zero");
        whenTrigonometricOperationWithValue("halfPi");
        whenTrigonometricOperationWithValue("pi");
        whenTrigonometricOperationWithValue("threeHalvesPi");

        thenResultsEqualsExpectedValue("zero", Math.cos(0));
        thenResultsEqualsExpectedValue("halfPi", Math.cos(Math.PI / 2));
        thenResultsEqualsExpectedValue("pi", Math.cos(Math.PI));
        thenResultsEqualsExpectedValue("threeHalvesPi", Math.cos(Math.PI * 1.5));
    }

    @Test
    public void testTanOperation() throws Exception {

        givenTrigonometricOperation("TAN");

        whenTrigonometricOperationWithValue("zero");
        whenTrigonometricOperationWithValue("quarterPi");
        whenTrigonometricOperationWithValue("pi");

        thenResultsEqualsExpectedValue("zero", Math.tan(0));
        thenResultsEqualsExpectedValue("quarterPi", Math.tan(Math.PI / 4));
        thenResultsEqualsExpectedValue("pi", Math.tan(Math.PI));
    }

    @Test
    public void testArcSinOperation() throws Exception {

        givenTrigonometricOperation("ASIN");

        whenTrigonometricOperationWithValue("zero");
        whenTrigonometricOperationWithValue("half");
        whenTrigonometricOperationWithValue("one");

        thenResultsEqualsExpectedValue("zero", Math.asin(0));
        thenResultsEqualsExpectedValue("half", Math.asin(0.5));
        thenResultsEqualsExpectedValue("one", Math.asin(1));

    }

    @Test
    public void testArcCosOperation() throws Exception {

        givenTrigonometricOperation("ACOS");

        whenTrigonometricOperationWithValue("zero");
        whenTrigonometricOperationWithValue("half");
        whenTrigonometricOperationWithValue("one");

        thenResultsEqualsExpectedValue("zero", Math.acos(0));
        thenResultsEqualsExpectedValue("half", Math.acos(0.5));
        thenResultsEqualsExpectedValue("one", Math.acos(1));

    }

    @Test
    public void testArcTanOperation() throws Exception {
        givenTrigonometricOperation("ATAN");

        whenTrigonometricOperationWithValue("zero");
        whenTrigonometricOperationWithValue("one");

        thenResultsEqualsExpectedValue("zero", Math.atan(0));
        thenResultsEqualsExpectedValue("one", Math.atan(1));
    }

    private void givenWireGraphConfiguration() throws KuraException {
        this.wireGraphConfiguration = wireGraphService.get();
    }

    private void givenTrigonometricOperation(String operation)
            throws InterruptedException, ExecutionException, TimeoutException {

        Map<String, Object> props = new HashMap<>();
        props.put(TRIGONOMETRIC_OPERATION, operation);
        WireTestUtil.updateWireComponentConfiguration(configurationService, this.activeWirePid, props).get(5,
                TimeUnit.SECONDS);
    }

    private void whenTrigonometricOperationWithValue(String operandName)
            throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<WireEnvelope> out0Recfuture = outReceiver.nextEnvelope();
        Optional<OperandTriple<String, String, TypedValue<Double>>> operandTriple = operandTriples.stream()
                .filter(operand -> operand.getFirstArgument().equals(operandName))
                .findFirst();

        if (operandTriple.isPresent()) {
            Map<String, TypedValue<?>> properties = new HashMap<>();
            properties.put(operandTriple.get().getSecondArgument(), operandTriple.get().getThirdArgument());
            inEmitter.emit(new WireRecord(properties));
            WireRecord receivedRecord = out0Recfuture.get(1, TimeUnit.SECONDS).getRecords().get(0);
            this.resultsMap.put(operandName,
                    (Double) receivedRecord.getProperties().get(RESULT_NAME_DEFAULT).getValue());
        } else {
            logger.error("No operand found for name: {}", operandName);
            throw new IllegalArgumentException("No operand found for name: " + operandName);
        }
    }

    private void thenWireGraphExistsWithPid(String expectedPid) {
        assertTrue(this.wireGraphConfiguration.getWireComponentConfigurations().stream()
                .anyMatch(wcc -> wcc.getConfiguration().getPid().equals(expectedPid)));
    }

    private void thenWireGraphConfigurationIsDefaultOne() {
        assertTrue(this.wireGraphConfiguration.getWireComponentConfigurations().stream()
                .anyMatch(wcc -> matchesDefaultConfiguration(wcc.getConfiguration())));
    }

    private void thenResultsEqualsExpectedValue(String operandName, Double expectedValue) {
        assertEquals(expectedValue, this.resultsMap.get(operandName));
    }

    private boolean matchesDefaultConfiguration(ComponentConfiguration cc) {
        if (cc.getPid().equals(this.activeWirePid)) {
            Map<String, Object> props = cc.getConfigurationProperties();
            return PARAMETER_NAME_DEFAULT.equals(props.get(PARAMETER_NAME_PROP_NAME))
                    && RESULT_NAME_DEFAULT.equals(props.get(RESULT_NAME_PROP_NAME))
                    && EMIT_RECEIVED_PROPERTIES_DEFAULT == (boolean) props.get(EMIT_RECEIVED_PROPERTIES)
                    &&
                    TRIGONOMETRIC_OPERATION_DEFAULT.equals(props.get(TRIGONOMETRIC_OPERATION));
        }
        return false;
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
}
