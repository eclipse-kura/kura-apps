package org.eclipse.kura.example.factory.bundle.unit.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.eclipse.kura.example.factory.component.FactoryComponentExample;
import org.eclipse.kura.example.factory.component.FactoryComponentExampleOCD;
import org.eclipse.kura.example.factory.component.FactoryComponentExampleOptions;
import org.junit.Test;
import org.mockito.Mockito;

public class FactoryComponentTest {

    FactoryComponentExample componentExample;
    FactoryComponentExampleOCD ocd;

    @Test
    public void shouldCreateCorrectOptions() throws IOException {
        givenComponent();
        givenOptions(1234, "Hi:I'm Test", false);

        whenUpdated();

        thenOptionsAreEqualsToCreatedOcd();

    }

    private void givenComponent() {
        this.componentExample = new FactoryComponentExample();
    }

    private void givenOptions(Integer tcpPort, String availableMessages, Boolean caseSensitive) {
        this.ocd = Mockito.mock(FactoryComponentExampleOCD.class);
        when(this.ocd.tcp_port()).thenReturn(tcpPort);
        when(this.ocd.available_messages()).thenReturn(availableMessages);
        when(this.ocd.case_sensitive()).thenReturn(caseSensitive);
    }

    private void whenUpdated() throws IOException {
        this.componentExample.updated(ocd);
    }

    private void thenOptionsAreEqualsToCreatedOcd() {
        FactoryComponentExampleOptions opts = new FactoryComponentExampleOptions(this.ocd);
        assertTrue(this.componentExample.getOptions().equals(opts));
        assertEquals(opts.hashCode(), this.componentExample.getOptions().hashCode());
    }
}
