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
package org.eclipse.kura.example.driver.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.channel.ChannelFlag;
import org.eclipse.kura.channel.ChannelRecord;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.driver.Driver;
import org.eclipse.kura.driver.Driver.ConnectionException;
import org.eclipse.kura.example.driver.ExampleDriverChannelDescriptor.UnitMeasure;
import org.eclipse.kura.type.DataType;
import org.eclipse.kura.util.wire.test.WireTestUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class ExampleComponentItTest {

    private static final Logger logger = LoggerFactory.getLogger(ExampleDriverTest.class);

    private static final String FACTORY_PID = "org.eclipse.kura.example.driver.ExampleDriver";

    static ConfigurationService configurationService;
    String activePid = null;
    Driver driver;
    List<ChannelRecord> channelList = new ArrayList<>();

    @Test
    public void shouldReadCorrectly()
            throws InterruptedException, ExecutionException, TimeoutException, ConnectionException {

        givenDriver();
        givenChannelWithConfig("Channel-1", DataType.DOUBLE, UnitMeasure.INCH, 10.5D);

        whenRead();

        thenDriverIsNotNull();
        thenChannelIsSuccessful("Channel-1");
        thenChannelValueIsCorrect("Channel-1", DataType.DOUBLE, 413.38457999999997);

    }

    @BeforeClass
    public static void setupConfigurationService()
            throws InterruptedException, ExecutionException, TimeoutException, KuraException, InvalidSyntaxException {
        configurationService = WireTestUtil.trackService(ConfigurationService.class, Optional.empty()).get(30,
                TimeUnit.SECONDS);
    }

    private void givenDriver() throws InterruptedException, ExecutionException, TimeoutException {
        this.activePid = "ExampleDriver";

        this.driver = WireTestUtil.createFactoryConfiguration(configurationService, Driver.class, activePid,
                FACTORY_PID, new HashMap<String, Object>()).get(30, TimeUnit.SECONDS);
    }

    private void givenChannelWithConfig(String channelName, DataType dataType, UnitMeasure unitMeasure,
            Double inputData) {

        ChannelRecord chRecord = ChannelRecord.createReadRecord(channelName, dataType);

        HashMap<String, Object> conf = new HashMap<>();
        conf.put("+type", "READ");
        conf.put("readChannel", "readChannel");
        conf.put("+name", channelName);
        conf.put("+value.type", dataType.toString());
        conf.put("output.unit.measure", unitMeasure.name());
        conf.put("input.data", inputData);

        chRecord.setChannelConfig(conf);

        this.channelList.add(chRecord);
    }

    private void whenRead() throws ConnectionException {
        this.driver.read(this.channelList);
    }

    private void thenDriverIsNotNull() {
        assertNotNull(driver);
    }

    private void thenChannelIsSuccessful(String channelName) {
        this.channelList.stream().filter(channel -> channel.getChannelName().equals(channelName)).findFirst()
                .ifPresentOrElse(channel -> {
                    assertEquals(ChannelFlag.SUCCESS, channel.getChannelStatus().getChannelFlag());
                }, Assert::fail);
    }

    private void thenChannelValueIsCorrect(String channelName, DataType expectedType, Object expectedValue) {
        this.channelList.stream().filter(channel -> channel.getChannelName().equals(channelName)).findFirst()
                .ifPresentOrElse(channel -> {
                    assertEquals(expectedType, channel.getValueType());
                    assertEquals(expectedValue, channel.getValue().getValue());
                }, Assert::fail);

    }
}
