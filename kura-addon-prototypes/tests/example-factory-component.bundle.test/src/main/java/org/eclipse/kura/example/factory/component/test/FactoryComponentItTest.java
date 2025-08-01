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
 *******************************************************************************/
package org.eclipse.kura.example.factory.component.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.util.wire.test.WireTestUtil;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class FactoryComponentItTest {

    private static final Logger logger = LoggerFactory.getLogger(FactoryComponentItTest.class);

    private static final String FACTORY_PID = "org.eclipse.kura.example.factory.FactoryComponentExample";

    private static ConfigurationService configurationService;
    private String activePid = null;
    private ConfigurableComponent component;

    private Map<String, Object> properties = new HashMap<>();

    private ClientSimulator clientSimulator;

    @Test
    public void shouldRespondCorrectly()
            throws InterruptedException, ExecutionException, TimeoutException, KuraException {
        givenComponent();
        givenProperty("tcp.port", 7654);
        givenProperty("available.messages", "Hello:Hi, How are you?\nBye:See you later");
        givenProperty("case.sensitive", false);
        givenComponentUpdate();
        givenClientSimulator("localhost", 7654);

        whenClientSendsMessage("Bye");

        // the \n is inserted in the client simulator of tests: the source code doesn't generate the last \n
        thenResponseEquals("See you later\n");
    }

    @Test
    public void shouldSuggestsAvailableMessages()
            throws InterruptedException, ExecutionException, TimeoutException, KuraException {
        givenComponent();
        givenProperty("tcp.port", 4567);
        givenProperty("available.messages", "Hello:Hi, How are you?\nBye:See you later");
        givenProperty("case.sensitive", false);
        givenComponentUpdate();
        givenClientSimulator("localhost", 4567);

        whenClientSendsMessage("WrongMessage");

        thenResponseEquals("""
                Unknown request. Available ones are:
                \t- Hello
                \t- Bye
                Requests are not case sensitive, just ask something :)
                """);
    }

    @BeforeClass
    public static void setupConfigurationService()
            throws InterruptedException, ExecutionException, TimeoutException, KuraException, InvalidSyntaxException {
        configurationService = WireTestUtil.trackService(ConfigurationService.class, Optional.empty()).get(30,
                TimeUnit.SECONDS);
    }

    @After
    public void cleanFramework() throws InterruptedException, ExecutionException, TimeoutException {
        WireTestUtil.deleteFactoryConfiguration(configurationService, this.activePid).get(30, TimeUnit.SECONDS);
    }

    private void givenComponent() throws InterruptedException, ExecutionException, TimeoutException {

        this.activePid = "ExampleFactoryComponent";
        this.properties.clear();

        this.component = WireTestUtil.createFactoryConfiguration(configurationService, ConfigurableComponent.class,
                activePid, FACTORY_PID, new HashMap<String, Object>()).get(30, TimeUnit.SECONDS);
    }

    private void givenProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    private void givenComponentUpdate() throws KuraException, InterruptedException {
        Thread.sleep(5000); // Giving some time to stop socket
        configurationService.updateConfiguration(this.activePid, this.properties);
        Thread.sleep(5000); // Giving some time to start socket
    }

    private void givenClientSimulator(String host, int port) {
        this.clientSimulator = new ClientSimulator(host, port);
    }

    private void whenClientSendsMessage(String message) throws InterruptedException {
        Thread.sleep(5000);
        this.clientSimulator.sendMessage(message);
        Thread.sleep(5000);
    }

    private void thenResponseEquals(String responseExpected) {
        assertEquals(responseExpected, this.clientSimulator.getResponseMessage());
    }
}
