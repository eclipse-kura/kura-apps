/*******************************************************************************
 * Copyright (c) 2021, 2025 Eurotech and/or its affiliates and others
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
package org.eclipse.kura.example.tamper.detection;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.security.tamper.detection.TamperDetectionProperties;
import org.eclipse.kura.security.tamper.detection.TamperDetectionService;
import org.eclipse.kura.security.tamper.detection.TamperEvent;
import org.eclipse.kura.security.tamper.detection.TamperStatus;
import org.eclipse.kura.type.TypedValue;
import org.eclipse.kura.type.TypedValues;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, //
        enabled = true, //
        name = "org.eclipse.kura.example.tamper.detection.TamperDetectionServiceImpl", //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        service = { ConfigurableComponent.class, TamperDetectionService.class } //
)
@Designate(ocd = TamperDetectionServiceImplOCD.class, factory = true)
public class TamperDetectionServiceImpl implements TamperDetectionService, ConfigurableComponent {

    private static final Logger logger = LoggerFactory.getLogger(TamperDetectionServiceImpl.class);

    private static final String TAMPERED_KEY = "tampered";

    private EventAdmin eventAdmin;
    private ConfigurationService configurationService;

    private boolean isDeviceTampered = false;
    private Optional<Date> tamperInstant = Optional.empty();
    private String ownPid;

    @Reference(name = "ConfigurationService", //
            policy = ReferencePolicy.STATIC, //
            cardinality = ReferenceCardinality.MANDATORY //
    )
    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Reference(name = "EventAdmin", //
            policy = ReferencePolicy.STATIC, //
            cardinality = ReferenceCardinality.MANDATORY //
    )
    public void setEventAdmin(final EventAdmin eventAdmin) {
        this.eventAdmin = eventAdmin;
    }

    @Activate
    public void activate(final ComponentContext componentContext, TamperDetectionServiceImplOCD ocd) {
        TamperDetectionServiceImpl.logger.info("activating...");
        ownPid = (String) componentContext.getProperties().get("service.pid");
        setDeviceTampered(ocd.deviceTamperStatus());
        TamperDetectionServiceImpl.logger.info("activating...done");
    }

    @Deactivate
    public void deactivate() {
        TamperDetectionServiceImpl.logger.info("deactivating...");
        TamperDetectionServiceImpl.logger.info("deactivating...done");
    }

    @Modified
    public void update(final ComponentContext componentContext, TamperDetectionServiceImplOCD ocd) {
        TamperDetectionServiceImpl.logger.info("updating...");
        ownPid = (String) componentContext.getProperties().get("service.pid");
        setDeviceTampered(ocd.deviceTamperStatus());
        TamperDetectionServiceImpl.logger.info("updating...done");
    }

    @Override
    public String getDisplayName() {
        return "Simulated tamper detection " + ownPid;
    }

    @Override
    public TamperStatus getTamperStatus() {
        final Map<String, TypedValue<?>> properties = new HashMap<>();

        if (tamperInstant.isPresent()) {
            properties.put(TamperDetectionProperties.TIMESTAMP_PROPERTY_KEY.getValue(),
                    TypedValues.newLongValue(tamperInstant.get().getTime()));
        }

        return new TamperStatus(isDeviceTampered, properties);
    }

    @Override
    public void resetTamperStatus() throws KuraException {
        configurationService.updateConfiguration(ownPid,
                Collections.singletonMap(TamperDetectionServiceImpl.TAMPERED_KEY, false));
    }

    private void postTamperEvent() {
        final TamperEvent tamperEvent = new TamperEvent(ownPid, getTamperStatus());

        this.eventAdmin.postEvent(tamperEvent);
    }

    private void setDeviceTampered(final boolean isDeviceTampered) {

        final boolean stateChanged = this.isDeviceTampered ^ isDeviceTampered;

        this.isDeviceTampered = isDeviceTampered;

        if (isDeviceTampered) {
            tamperInstant = Optional.of(new Date());
        } else {
            tamperInstant = Optional.empty();
        }

        if (isDeviceTampered || stateChanged) {
            postTamperEvent();
        }
    }
}
