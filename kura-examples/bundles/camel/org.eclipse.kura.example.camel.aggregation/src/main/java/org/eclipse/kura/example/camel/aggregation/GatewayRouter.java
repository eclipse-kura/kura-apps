/*******************************************************************************
 * Copyright (c) 2016, 2025 Red Hat Inc and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *  Red Hat Inc
 *******************************************************************************/
package org.eclipse.kura.example.camel.aggregation;

import static java.util.concurrent.TimeUnit.SECONDS;

import org.eclipse.kura.camel.component.AbstractJavaCamelComponent;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example of the Kura Camel application.
 */
@Component(immediate = true, //
        enabled = true, //
        name = "org.eclipse.kura.example.camel.aggregation.GatewayRouter", //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        service = { ConfigurableComponent.class } //
)
@Designate(ocd = GatewayRouterOCD.class, factory = true)
public class GatewayRouter extends AbstractJavaCamelComponent {

    private static final Logger logger = LoggerFactory.getLogger(GatewayRouter.class);

    private static final int DEFAULT_MINIMUM = 0;
    private static final int DEFAULT_MAXIMUM = 40;

    private RandomTemperatureGenerator randomTemperatureGenerator = new RandomTemperatureGenerator(DEFAULT_MINIMUM,
            DEFAULT_MAXIMUM);

    @Override
    public void configure() throws Exception {
        from("timer://temperature") //
                .setBody(this.randomTemperatureGenerator) //
                .aggregate(simple("temperature"), new AverageAggregationStrategy()) //
                .completionInterval(SECONDS.toMillis(10)) //
                .to("log:averageTemperatureFromLast10Seconds");
    }

    @Activate
    protected void activate(final ComponentContext componentContext, GatewayRouterOCD ocd)
            throws Exception {
        logger.info("Activated");

        setProperties(ocd);
        start();
    }

    @Modified
    protected void modified(GatewayRouterOCD ocd) {
        logger.info("Modified");

        setProperties(ocd);
    }

    @Deactivate
    protected void deactivate() throws Exception {
        stop();
    }

    private void setProperties(GatewayRouterOCD ocd) {
        int minimum = ocd.minimum();
        int maximum = ocd.maximum();

        if (maximum - minimum <= 0) {
            throw new IllegalArgumentException("Maximum must be at least one higher than minimum");
        }

        this.randomTemperatureGenerator.setMinimum(minimum);
        this.randomTemperatureGenerator.setMaximum(maximum);
    }
}
