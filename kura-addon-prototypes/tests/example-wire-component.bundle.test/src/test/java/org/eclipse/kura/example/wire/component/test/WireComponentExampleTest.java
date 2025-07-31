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
package org.eclipse.kura.example.wire.component.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.kura.example.wire.component.WireComponentExample;
import org.eclipse.kura.example.wire.component.WireComponentExampleOCD;
import org.eclipse.kura.example.wire.component.WireComponentExampleOptions;
import org.eclipse.kura.type.TypedValue;
import org.eclipse.kura.type.TypedValues;
import org.eclipse.kura.wire.WireEnvelope;
import org.eclipse.kura.wire.WireHelperService;
import org.eclipse.kura.wire.WireRecord;
import org.eclipse.kura.wire.WireSupport;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.wireadmin.Wire;

public class WireComponentExampleTest {

    private WireComponentExample exampleComponent = new WireComponentExample();
    private Map<String, Object> properties = new HashMap<>();
    WireRecord wireRecord;
    Map<String, TypedValue<?>> wireRecordProps = new HashMap<>();

    Exception exception = null;

    String onWireResult = "";

    String onWireResultTemplate = "You choose the channel %s, whose value is %s";

    @After
    public void deactivateComponent() {
        this.exampleComponent.deactivate();
    }

    @Test
    public void shouldSetOptionsCorrectly() {
        givenExampleComponent();
        givenProperties("channel.filter.name", "Channel-1");

        whenActivate();

        thenOptionsEquals("Channel-1");
    }

    private void thenOptionsEquals(String expectedChannelOption) {
        WireComponentExampleOCD ocd = Mockito.mock(WireComponentExampleOCD.class);
        when(ocd.channel_filter_name()).thenReturn(expectedChannelOption);
        WireComponentExampleOptions options = new WireComponentExampleOptions(ocd);

        assertEquals(options.getChannelFilterName(), this.exampleComponent.getOptions().getChannelFilterName());
    }

    @Test
    public void shouldActivate() {

        givenExampleComponent();
        givenProperties("channel.filter.name", "Channel-1");
        givenWireRecordProp("Channel-1", TypedValues.newIntegerValue(10));
        givenWireEnvelope(this.wireRecordProps);

        whenActivate();
        whenOnWireReceived();

        thenNoExceptionCaught();
        thenResultCorrect("Channel-1", "10");
    }

    @Test
    public void shouldNotFailEvenWithoutChannels() {
        givenExampleComponent();
        givenProperties("channel.filter.name", "Channel-1");
        givenWireEnvelope(this.wireRecordProps);

        whenActivate();
        whenOnWireReceived();

        thenNoExceptionCaught();
    }

    private void givenExampleComponent() {
        this.exception = null;
        this.properties.clear();
        this.wireRecord = null;
        this.wireRecordProps.clear();
        this.exampleComponent = new WireComponentExample();
    }

    private void givenProperties(String key, Object value) {
        this.properties.put(key, value);
    }

    private void givenWireRecordProp(String key, TypedValue<?> value) {
        this.wireRecordProps.put(key, value);
    }

    private void givenWireEnvelope(Map<String, TypedValue<?>> props) {
        this.wireRecord = new WireRecord(props);
    }

    private void whenActivate() {
        WireComponentExampleOCD ocd = Mockito.mock(WireComponentExampleOCD.class);
        when(ocd.channel_filter_name()).thenReturn("Channel-1");
        ComponentContext mockContext = mock(ComponentContext.class);

        WireHelperService helperService = mock(WireHelperService.class);
        when(helperService.newWireSupport(any(), any())).thenReturn(new MockWireSupport());
        exampleComponent.setWireHelperService(helperService);

        this.exampleComponent.activate(mockContext, ocd);
    }

    private void whenOnWireReceived() {
        try {
            this.exampleComponent.onWireReceive(new WireEnvelope("test", Arrays.asList(this.wireRecord)));
        } catch (Exception ex) {
            this.exception = ex;
        }
    }

    private void thenNoExceptionCaught() {
        assertNull(this.exception);
    }

    private void thenResultCorrect(String channelNameTemplate, String channelValueTemplate) {
        assertEquals(String.format(this.onWireResultTemplate, channelNameTemplate, channelValueTemplate),
                this.onWireResult);
    }

    private class MockWireSupport implements WireSupport {

        @Override
        public Object polled(Wire wire) {
            return null;
        }

        @Override
        public void consumersConnected(Wire[] wires) {
            // do nothing

        }

        @Override
        public void updated(Wire wire, Object value) {
            // do nothing

        }

        @Override
        public void producersConnected(Wire[] wires) {
            // do nothing

        }

        @Override
        public void emit(List<WireRecord> wireRecords) {
            onWireResult = (String) wireRecords.get(0).getProperties().get("Emitted Value").getValue();
        }

    }

}