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

package org.eclipse.kura.example.wire.math.singleport.gainoffset.test;

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

public class GainOffsetComponentTest {

    private static final Logger logger = LoggerFactory.getLogger(GainOffsetComponentTest.class);

    private static final String FACTORY_PID = "org.eclipse.kura.example.wire.math.singleport.gainoffset.GainOffsetComponent";
    private static final String UNDER_TEST_PID = "under.test";
    private static final String TEST_EMITTER_PID = "test.emitter.pid";
    private static final String TEST_RECEIVER_PID = "test.receiver.pid";

    // in ports
    private static final int IN_PORT = 0;

    // out ports
    private static final int OUT_PORT = 0;

    // configuration properties of component under test
    private static final String CONFIGURATION_PROP_NAME = "configuration";
    private static final String EMIT_RECEIVED_PROPERTIES = "emit.received.properties";

    private static final String CONFIGURATION_PROP_NAME_DEFAULT = "toBeMultipliedByTwo | 2;toBeMultipliedBy3AndIncreasedBy1 | 3 | 1";
    private static final boolean EMIT_RECEIVED_PROPERTIES_DEFAULT = false;

    private static WireGraphService wireGraphService;
    private static ConfigurationService configurationService;

    private static TestEmitterReceiver outReceiver;
    private static TestEmitterReceiver inEmitter;

    final GraphBuilder builder = new GraphBuilder();
    final BundleContext bundleContext = FrameworkUtil.getBundle(GainOffsetComponentTest.class).getBundleContext();

    //
    // JUnit 4 stuff
    @BeforeClass
    public static void setUpOnce() throws InterruptedException {
        // Wait for OSGi dependencies
        logger.info("waiting for dependencies...");

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

        builder.addWireComponent(UNDER_TEST_PID, FACTORY_PID, 1, 1) //
                .addTestEmitterReceiver(TEST_EMITTER_PID) //
                .addTestEmitterReceiver(TEST_RECEIVER_PID) //
                .addWire(TEST_EMITTER_PID, 0, UNDER_TEST_PID, IN_PORT) //
                .addWire(UNDER_TEST_PID, OUT_PORT, TEST_RECEIVER_PID, 0);

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
    public void gainOffsetWireComponentExists() throws KuraException {
        WireGraphConfiguration wgc;
        try {
            wgc = wireGraphService.get();
        } catch (KuraException e) {
            logger.error("Test error", e);
            throw e;
        }

        assertTrue(wgc.getWireComponentConfigurations().stream()
                .anyMatch(wcc -> UNDER_TEST_PID.equals(wcc.getConfiguration().getPid())));
    }

    @Test
    public void gainOffsetWireComponentHasDefaultProperties() throws KuraException {
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
        if (cc.getPid().equals(UNDER_TEST_PID)) {
            Map<String, Object> props = cc.getConfigurationProperties();
            return CONFIGURATION_PROP_NAME_DEFAULT.equals(props.get(CONFIGURATION_PROP_NAME))
                    && EMIT_RECEIVED_PROPERTIES_DEFAULT == (boolean) props.get(EMIT_RECEIVED_PROPERTIES);
        }
        return false;
    }

    @Test
    public void testGainOffset() throws Exception {
        logger.info("### TESTING GAIN OFFSET COMPONENT ###");
        Map<String, Object> props = new HashMap<>();

        try {
            WireTestUtil.updateWireComponentConfiguration(configurationService, UNDER_TEST_PID, props).get(30,
                    TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            logger.error("Test error", e);
            throw e;
        }

        CompletableFuture<WireEnvelope> out0Recfuture = outReceiver.nextEnvelope();

        Map<String, TypedValue<?>> myMap = new HashMap<>();
        myMap.put("toBeMultipliedByTwo", TypedValues.newDoubleValue(4));
        myMap.put("toBeMultipliedBy3AndIncreasedBy1", TypedValues.newDoubleValue(2));
        inEmitter.emit(new WireRecord(myMap));

        try {
            WireRecord receivedRecord = out0Recfuture.get(1, TimeUnit.SECONDS).getRecords().get(0);
            logger.info("received {}", receivedRecord.getProperties());
            assertTrue((double) receivedRecord.getProperties().get("toBeMultipliedByTwo").getValue() == 8);
            assertTrue((double) receivedRecord.getProperties().get("toBeMultipliedBy3AndIncreasedBy1").getValue() == 7);

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
