/*******************************************************************************
 * Copyright (c) 2019, 2020 Eurotech and/or its affiliates and others
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

package org.eclipse.kura.example.gpio.led;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.gpio.GPIOService;
import org.eclipse.kura.gpio.KuraClosedDeviceException;
import org.eclipse.kura.gpio.KuraGPIODeviceException;
import org.eclipse.kura.gpio.KuraGPIOPin;
import org.eclipse.kura.gpio.KuraUnavailableDeviceException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, //
        enabled = true, //
        name = "org.eclipse.kura.example.gpio.led.LedExample", //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        service = { ConfigurableComponent.class } //
)

@Designate(ocd = LedExampleOCD.class, factory = false)
public class LedExample implements ConfigurableComponent {

    private static final Logger logger = LoggerFactory.getLogger(LedExample.class);
    private static final String APP_ID = "org.eclipse.kura.example.gpio.led.LedExample";

    private GPIOService myservice;
    private KuraGPIOPin pin;

    @Reference( //
            cardinality = ReferenceCardinality.MANDATORY, //
            policy = ReferencePolicy.STATIC, //
            name = "GPIOService", //
            unbind = "unbindGPIOService" //
    )
    protected synchronized void bindGPIOService(final GPIOService gpioService) {
        this.myservice = gpioService;
    }

    protected synchronized void unbindGPIOService(final GPIOService gpioService) {
        this.myservice = null;
    }

    @Activate
    protected void activate(final LedExampleOCD ocd) {
        LedExample.logger.info("Bundle {} has started with config!", LedExample.APP_ID);
        updated(ocd);
    }

    @Deactivate
    protected void deactivate(final LedExampleOCD ocd) {
        LedExample.logger.info("Bundle {} has stopped!", LedExample.APP_ID);
        close();
    }

    @Modified
    public void updated(final LedExampleOCD ocd) {

        close();

        pin = this.myservice.getPinByTerminal(ocd.configure_pin());

        if (pin == null) {
            return;
        }

        open();

        setValue(ocd.led_state());

    }

    private void open() {
        try {
            pin.open();
        } catch (KuraGPIODeviceException | KuraUnavailableDeviceException | IOException e) {
            LedExample.logger.error("Open Exception ", e);
        }
    }

    private void close() {

        if (pin != null) {
            try {
                pin.close();
            } catch (final IOException e) {
                LedExample.logger.error("Close Exception ", e);
            }
        }
    }

    private void setValue(final boolean bool) {
        try {
            pin.setValue(bool);
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException | KuraUnavailableDeviceException | IOException | KuraClosedDeviceException e) {
            LedExample.logger.error("Set Value Exception ", e);
        }
    }
}
