package org.eclipse.kura.wire.devel.sink;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.configuration.ComponentConfiguration;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.type.TypedValue;
import org.eclipse.kura.type.TypedValues;
import org.eclipse.kura.util.wire.test.GraphBuilder;
import org.eclipse.kura.util.wire.test.TestEmitterReceiver;
import org.eclipse.kura.util.wire.test.WireTestUtil;
import org.eclipse.kura.wire.WireRecord;
import org.eclipse.kura.wire.graph.WireGraphConfiguration;
import org.eclipse.kura.wire.graph.WireGraphService;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SinkComponentTest {

    private static final Logger logger = LoggerFactory.getLogger(SinkComponentTest.class);

    private static final String FACTORY_PID = "org.eclipse.kura.wire.devel.sink.Sink";
    private static final String TEST_EMITTER_PID = "test.emitter.pid";

    // in ports
    private static final int IN_PORT = 0;

    private static TestEmitterReceiver inEmitter;

    // configuration properties of component under test
    private static final String MEASURE_TIMINGS_NAME_PROP_NAME = "measure.timings";

    private static final Boolean MEASURE_TIMINGS_NAME_DEFAULT = true;

    private static WireGraphService wireGraphService;
    private static ConfigurationService configurationService;

    private ConfigurableComponent configurableComponent;

    private final GraphBuilder builder = new GraphBuilder();
    private WireGraphConfiguration wireGraphConfiguration;
    private final BundleContext bundleContext = FrameworkUtil.getBundle(SinkComponentTest.class)
            .getBundleContext();

    String activeWirePid;

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

        builder.addWireComponent(this.activeWirePid, FACTORY_PID, 1, 0) //
                .addTestEmitterReceiver(TEST_EMITTER_PID) //
                .addWire(TEST_EMITTER_PID, 0, this.activeWirePid, IN_PORT);

        try {
            builder.replaceExistingGraph(bundleContext, wireGraphService).get(30, TimeUnit.SECONDS);

            inEmitter = builder.getTrackedWireComponent(TEST_EMITTER_PID);
        } catch (KuraException | ExecutionException e) {
            logger.error("Test error", e);
            throw e;
        }
    }

    @Test
    public void sinkWireComponentExists() throws KuraException {
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
    public void sinkWireComponentHasDefaultProperties() throws KuraException {
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
            return MEASURE_TIMINGS_NAME_DEFAULT && (boolean) props.get(MEASURE_TIMINGS_NAME_PROP_NAME);
        }
        return false;
    }

    @Test
    public void testSink() throws KuraException, InterruptedException {

        // This tests trigger the emit method that only writes to log. So we just check
        // that the graph still exists after the emit.
        logger.info("### TESTING SINK COMPONENT ###");
        Map<String, TypedValue<?>> myMap = new HashMap<>();

        myMap.put("value", TypedValues.newIntegerValue(2));
        inEmitter.emit(new WireRecord(myMap));
        Thread.sleep(2000);
        inEmitter.emit(new WireRecord(myMap));

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
