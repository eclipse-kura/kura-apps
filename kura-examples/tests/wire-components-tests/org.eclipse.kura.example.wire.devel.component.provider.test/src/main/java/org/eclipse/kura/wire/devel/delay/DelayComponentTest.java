package org.eclipse.kura.wire.devel.delay;

import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.configuration.ComponentConfiguration;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.util.wire.test.GraphBuilder;
import org.eclipse.kura.util.wire.test.TestEmitterReceiver;
import org.eclipse.kura.util.wire.test.WireTestUtil;
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

public class DelayComponentTest {

    private static final Logger logger = LoggerFactory.getLogger(DelayComponentTest.class);

    private static final String FACTORY_PID = "org.eclipse.kura.wire.devel.delay.Delay";
    private static final String TEST_EMITTER_PID = "test.emitter.pid";
    private static final String TEST_RECEIVER_PID = "test.receiver.pid";

    // in ports
    private static final int IN_PORT = 0;

    // out ports
    private static final int OUT_PORT = 0;

    private static TestEmitterReceiver inEmitter;
    private static TestEmitterReceiver outReceiver;

    // configuration properties of component under test
    private static final String DELAY_AVERAGE_NAME_PROP_NAME = "delay.average";
    private static final String DELAY_STANDARD_DEVIATION_NAME_PROP_NAME = "delay.standard.deviation";

    private static final int DELAY_AVERAGE_NAME_DEFAULT = 1000;
    private static final int DELAY_STANDARD_DEVIATION_NAME_DEFAULT = 100;

    private static WireGraphService wireGraphService;
    private static ConfigurationService configurationService;

    private ConfigurableComponent configurableComponent;

    private final GraphBuilder builder = new GraphBuilder();
    private WireGraphConfiguration wireGraphConfiguration;
    private final BundleContext bundleContext = FrameworkUtil.getBundle(DelayComponentTest.class)
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
    public void maximumWireComponentExists() throws KuraException {
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
    public void maximumWireComponentHasDefaultProperties() throws KuraException {
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
            return DELAY_AVERAGE_NAME_DEFAULT == (int) props.get(DELAY_AVERAGE_NAME_PROP_NAME)
                    && DELAY_STANDARD_DEVIATION_NAME_DEFAULT == (int) props
                            .get(DELAY_STANDARD_DEVIATION_NAME_PROP_NAME);
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
