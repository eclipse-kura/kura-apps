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
package org.eclipse.kura.example.factory.component;

import java.io.IOException;
import java.util.Objects;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        name = "org.eclipse.kura.example.factory.FactoryComponentExample", //
        service = { ConfigurableComponent.class } //
)
@Designate(ocd = FactoryComponentExampleOCD.class, factory = true)
public class FactoryComponentExample implements ConfigurableComponent {

    private static final Logger logger = LoggerFactory.getLogger(FactoryComponentExample.class);

    private FactoryComponentExampleOptions options;

    private ClientHandler clientHandler;

    /*
     * In the in activate, modified, deactivate methods it is possible to provide
     * the ComponentContext and the ExampleComponentOCD as parameters.
     * All parameters in activate, modified, deactivate are optional and can be
     * removed if not needed
     * 
     * Examples:
     * 
     * public void activate()
     * public void activate(ExampleComponentOCD configuration)
     * public void activate(ComponentContext componentContext, final Map<String, Object> properties, final
     * ExampleComponentOCD configuration)
     */
    @Activate
    public void activate(final FactoryComponentExampleOCD properties) throws IOException {
        logger.info("Activating");

        updated(properties);

        logger.info("Activated");
    }

    @Modified
    public void updated(final FactoryComponentExampleOCD properties) throws IOException {
        logger.info("Updating");

        logger.debug("Updating with properties: {}", properties);

        FactoryComponentExampleOptions newOptions = new FactoryComponentExampleOptions(properties);

        if (!Objects.equals(this.options, newOptions)) {

            if (this.clientHandler != null) {
                this.clientHandler.stopSocket();
            }

            try {
                this.options = newOptions;

                this.clientHandler = new ClientHandler(newOptions);
                this.clientHandler.buildAndRunSocket();

            } catch (Exception ex) {
                logger.error(ex.getMessage());
            }

        }

        logger.info("Updated");

    }

    @Deactivate
    public synchronized void deactivate() {
        logger.info("Deactivating");
        logger.info("Deactivated");
    }

    public FactoryComponentExampleOptions getOptions() {
        return this.options;
    }

}
