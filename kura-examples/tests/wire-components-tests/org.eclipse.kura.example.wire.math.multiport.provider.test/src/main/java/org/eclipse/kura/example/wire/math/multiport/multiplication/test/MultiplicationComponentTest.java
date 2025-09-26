package org.eclipse.kura.example.wire.math.multiport.multiplication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import org.eclipse.kura.example.wire.math.multiport.difference.test.DifferenceComponentTest;
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

public class MultiplicationComponentTest {

    private static final Logger logger = LoggerFactory.getLogger(MultiplicationComponentTest.class);

    private static final String FACTORY_PID = "org.eclipse.kura.example.wire.math.multiport.multiplication.MultiplicationComponent";
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
    private static final String RESULT_NAME_PROPERTY_NAME = "result.name";

    private static final String OPERAND_NAME_DEFAULT = "operand";
    private static final String RESULT_NAME_DEFAULT = "result";
    private static final boolean BARRIER_MODALITY_PROPERTY_DEFAULT = true;

    private static ConfigurationService configurationService;
    private static WireGraphService wireGraphService;

    private ConfigurableComponent configurableComponent;

    private final GraphBuilder builder = new GraphBuilder();
    private WireGraphConfiguration wireGraphConfiguration;
    private final BundleContext bundleContext = FrameworkUtil.getBundle(DifferenceComponentTest.class)
            .getBundleContext();

    String activeWirePid;

    double envelopeResult;

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
    public void shouldReturnCorrectDivision() throws InterruptedException, ExecutionException, TimeoutException {
        givenMultiplicationOperation(10, 5);

        thenResultEquals(50);
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

    private void givenWireComponent() {
        this.configurableComponent = builder.getTrackedWireComponent(this.activeWirePid);
    }

    private void givenWireGraphConfiguration() throws KuraException {
        this.wireGraphConfiguration = wireGraphService.get();
    }

    private void givenMultiplicationOperation(int firstOperand, int secondOperand)
            throws InterruptedException, ExecutionException, TimeoutException {
        in0emitter.emit(new WireRecord(Map.of(OPERAND_NAME_DEFAULT, TypedValues.newIntegerValue(firstOperand))));
        CompletableFuture<WireEnvelope> out0Recfuture = outReceiver.nextEnvelope();
        in1emitter.emit(new WireRecord(Map.of(OPERAND_NAME_DEFAULT, TypedValues.newIntegerValue(secondOperand))));
        WireRecord receivedRecord = out0Recfuture.get(1, TimeUnit.SECONDS).getRecords().get(0);
        this.envelopeResult = (double) receivedRecord.getProperties().get(RESULT_NAME_DEFAULT).getValue();

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

    private void thenResultEquals(double expectedResult) {
        assertEquals(expectedResult, this.envelopeResult, 0.00001);
    }

    private boolean matchesDefaultConfiguration(ComponentConfiguration cc) {
        if (cc.getPid().equals(this.activeWirePid)) {
            Map<String, Object> props = cc.getConfigurationProperties();
            return OPERAND_NAME_DEFAULT.equals(props.get(FIRST_OPERAND_NAME_PROP_NAME))
                    && OPERAND_NAME_DEFAULT.equals(props.get(SECOND_OPERAND_NAME_PROP_NAME))
                    && BARRIER_MODALITY_PROPERTY_DEFAULT == (boolean) props.get(BARRIER_MODALITY_PROPERTY_KEY)
                    && RESULT_NAME_DEFAULT.equals(props.get(RESULT_NAME_PROPERTY_NAME));
        }
        return false;
    }
}
