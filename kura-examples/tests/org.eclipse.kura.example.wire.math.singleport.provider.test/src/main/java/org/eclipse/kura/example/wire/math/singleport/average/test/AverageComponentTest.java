/*******************************************************************************
 * Copyright (c) 2020 Eurotech and/or its affiliates and others
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

package org.eclipse.kura.example.wire.math.singleport.average.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.configuration.ComponentConfiguration;
import org.eclipse.kura.configuration.ConfigurationService;
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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AverageComponentTest {

    // See:
    // http://stackoverflow.com/questions/7161338/using-osgi-declarative-services-in-the-context-of-a-junit-test

    private static final Logger logger = LoggerFactory.getLogger(AverageComponentTest.class);

    private static final String FACTORY_PID = "org.eclipse.kura.example.wire.math.singleport.average.AverageComponent";
    private static final String TEST_EMITTER_PID = "test.emitter.pid";
    private static final String TEST_RECEIVER_PID = "test.receiver.pid";

    // in ports
    private static final int IN_PORT = 0;

    // out ports
    private static final int OUT_PORT = 0;

    // configuration properties of component under test
    private static final String OPERAND_NAME_PROP_NAME = "operand.name";
    private static final String RESULT_NAME_PROP_NAME = "result.name";
    private static final String EMIT_RECEIVED_PROPERTIES = "emit.received.properties";
    private static final String WINDOW_SIZE_PROP_NAME = "window.size";

    private static final String OPERAND_NAME_DEFAULT = "operand";
    private static final String RESULT_NAME_DEFAULT = "result";
    private static final boolean EMIT_RECEIVED_PROPERTIES_DEFAULT = false;
    private static final int WINDOW_SIZE_DEFAULT = 10;

    private static WireGraphService wireGraphService;
    private static ConfigurationService configurationService;

    private final GraphBuilder builder = new GraphBuilder();
    private final BundleContext bundleContext = FrameworkUtil.getBundle(AverageComponentTest.class).getBundleContext();

    String activeWirePid;

    private static TestEmitterReceiver outReceiver;
    private static TestEmitterReceiver inEmitter;

    public AverageComponentTest() {
        super();
        logger.info("{} created", System.identityHashCode(this));
    }

    //
    // JUnit 4 stuff
    @BeforeClass
    public static void setUpOnce() throws InterruptedException {
        try {
            configurationService = WireTestUtil.trackService(ConfigurationService.class, Optional.empty()).get(30,
                    TimeUnit.SECONDS);
            wireGraphService = WireTestUtil.trackService(WireGraphService.class, Optional.empty()).get(30,
                    TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup WireGraphService", e);
        }

    }

    @Before
    public void setUp() throws KuraException, InterruptedException, TimeoutException, ExecutionException {
        logger.info("{} setup", System.identityHashCode(this));

        this.activeWirePid = "underTestPid";

        builder.addWireComponent(this.activeWirePid, FACTORY_PID, 1, 1) //
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
    public void averageWireComponentExists() throws KuraException {
        WireGraphConfiguration wgc;
        try {
            wgc = wireGraphService.get();
        } catch (KuraException e) {
            logger.error("Test error", e);
            throw e;
        }

        assertTrue(wgc.getWireComponentConfigurations().stream()
                .anyMatch(wcc -> this.activeWirePid.equals(wcc.getConfiguration().getPid())));
    }

    @Test
    public void averageWireComponentHasDefaultProperties() throws KuraException {
        WireGraphConfiguration wgc;
        try {
            wgc = wireGraphService.get();
        } catch (KuraException e) {
            logger.error("Test error", e);
            throw e;
        }

        assertTrue(wgc.getWireComponentConfigurations().stream()
                .anyMatch(wcc -> matchesDefaultConfiguration(wcc.getConfiguration())));
    }

    private boolean matchesDefaultConfiguration(ComponentConfiguration cc) {
        if (cc.getPid().equals(this.activeWirePid)) {
            Map<String, Object> props = cc.getConfigurationProperties();
            return OPERAND_NAME_DEFAULT.equals(props.get(OPERAND_NAME_PROP_NAME))
                    && EMIT_RECEIVED_PROPERTIES_DEFAULT == (boolean) props.get(EMIT_RECEIVED_PROPERTIES)
                    && WINDOW_SIZE_DEFAULT == (int) props.get(WINDOW_SIZE_PROP_NAME)
                    && RESULT_NAME_DEFAULT.equals(props.get(RESULT_NAME_PROP_NAME));
        }
        return false;
    }

    @Test
    public void testOddaverage() throws Exception {
        logger.info("### TESTING ODD AVERAGE COMPONENT ###");
        Map<String, Object> props = new HashMap<>();
        props.put(WINDOW_SIZE_PROP_NAME, 9);
        try {
            WireTestUtil.updateWireComponentConfiguration(configurationService, this.activeWirePid, props).get(30,
                    TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            logger.error("Test error", e);
            throw e;
        }

        Map<String, TypedValue<?>> myMap = new HashMap<>();

        for (int i = 1; i < 9; i++) {
            myMap.clear();
            myMap.put(OPERAND_NAME_DEFAULT, TypedValues.newDoubleValue(i));
            inEmitter.emit(new WireRecord(myMap));
        }
        CompletableFuture<WireEnvelope> out0Recfuture = outReceiver.nextEnvelope();
        myMap.clear();
        myMap.put(OPERAND_NAME_DEFAULT, TypedValues.newDoubleValue(9));
        inEmitter.emit(new WireRecord(myMap));

        try {
            WireRecord receivedRecord = out0Recfuture.get(1, TimeUnit.SECONDS).getRecords().get(0);
            logger.info("received {}", receivedRecord.getProperties());
            assertTrue(((double) receivedRecord.getProperties().get(RESULT_NAME_DEFAULT).getValue() == 5));

        } catch (TimeoutException e) {
            fail("Timeout waiting for envelope");
        }
    }

    @Test
    public void testEvenaverage() throws Exception {
        logger.info("### TESTING EVEN AVERAGE COMPONENT ###");
        Map<String, Object> props = new HashMap<>();
        props.put(WINDOW_SIZE_PROP_NAME, 10);
        try {
            WireTestUtil.updateWireComponentConfiguration(configurationService, this.activeWirePid, props).get(30,
                    TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            logger.error("Test error", e);
            throw e;
        }
        Map<String, TypedValue<?>> myMap = new HashMap<>();
        for (int i = 1; i < 10; i++) {
            myMap.clear();
            myMap.put(OPERAND_NAME_DEFAULT, TypedValues.newDoubleValue(i));
            inEmitter.emit(new WireRecord(myMap));
        }
        CompletableFuture<WireEnvelope> out0Recfuture = outReceiver.nextEnvelope();
        myMap.clear();
        myMap.put(OPERAND_NAME_DEFAULT, TypedValues.newDoubleValue(10));
        inEmitter.emit(new WireRecord(myMap));

        try {
            WireRecord receivedRecord = out0Recfuture.get(1, TimeUnit.SECONDS).getRecords().get(0);
            logger.info("received {}", receivedRecord.getProperties());
            assertTrue(((double) receivedRecord.getProperties().get(RESULT_NAME_DEFAULT).getValue() == 5.5));

        } catch (TimeoutException e) {
            fail("Timeout waiting for envelope");
        }
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

    @AfterClass
    public static void tearDownOnce() {
        logger.info("tear down once");
    }
}
