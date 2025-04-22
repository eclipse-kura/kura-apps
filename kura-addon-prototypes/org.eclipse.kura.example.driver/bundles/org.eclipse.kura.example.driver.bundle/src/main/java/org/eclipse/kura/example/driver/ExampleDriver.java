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
package org.eclipse.kura.example.driver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.channel.ChannelFlag;
import org.eclipse.kura.channel.ChannelRecord;
import org.eclipse.kura.channel.ChannelStatus;
import org.eclipse.kura.channel.listener.ChannelListener;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.driver.ChannelDescriptor;
import org.eclipse.kura.driver.Driver;
import org.eclipse.kura.driver.PreparedRead;
import org.eclipse.kura.example.driver.conversion.MeasureConverter;
import org.eclipse.kura.type.DataType;
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
        property = {}, //
        service = { Driver.class, ConfigurableComponent.class }, //
        factory = "org.eclipse.kura.example.driver.factory" //
)
@Designate(ocd = ExampleDriverOCD.class, factory = false)
public class ExampleDriver implements ConfigurableComponent, Driver {

    private static final Logger logger = LoggerFactory.getLogger(ExampleDriver.class);

    private ExampleDriverOptions options;
    private List<ExampleDriverListener> channelListeners = new ArrayList<>();

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
    public void activate(final Map<String, Object> properties) {
        logger.info("Activating");

        updated(properties);

        logger.info("Activated");
    }

    @Modified
    public void updated(final Map<String, Object> properties) {
        logger.info("Updating");

        logger.debug("Updating with properties: {}", properties);
        this.options = new ExampleDriverOptions(properties);

        logger.info("Updated");
    }

    @Deactivate
    public synchronized void deactivate() {
        logger.info("Deactivating");
        logger.info("Deactivated");
    }

    public ExampleDriverOptions getOptions() {
        return this.options;
    }

    @Override
    public synchronized void connect() throws ConnectionException {
        logger.info("connecting...");
        logger.info("connecting...done");
    }

    @Override
    public synchronized void disconnect() throws ConnectionException {
        logger.info("disconnecting...");
        logger.info("disconnecting...done");
    }

    @Override
    public ChannelDescriptor getChannelDescriptor() {
        return ExampleDriverChannelDescriptor.instance();
    }

    @Override
    public void read(List<ChannelRecord> records) throws ConnectionException {

        for (ChannelRecord chRecord : records) {
            try {
                Double result = MeasureConverter.convertMeasure(this.options.getInputUnitMeasure(),
                        ExampleDriverChannelDescriptor.getInputData(chRecord.getChannelConfig()),
                        ExampleDriverChannelDescriptor.getOutputUnitMeasure(chRecord.getChannelConfig()));

                chRecord.setValue(MeasureConverter.convertToDataType(result, chRecord.getValueType()));
                chRecord.setChannelStatus(new ChannelStatus(ChannelFlag.SUCCESS));
                chRecord.setTimestamp(System.currentTimeMillis());
            } catch (Exception ex) {
                chRecord.setChannelStatus(new ChannelStatus(ChannelFlag.FAILURE, ex.getMessage(), null));
                chRecord.setTimestamp(System.currentTimeMillis());
            }
        }
    }

    @Override
    public void registerChannelListener(Map<String, Object> channelConfig, ChannelListener listener)
            throws ConnectionException {

        this.channelListeners.add(new ExampleDriverListener(channelConfig.get("+name").toString(),
                DataType.valueOf((String) channelConfig.get("+value.type")),
                ExampleDriverChannelDescriptor.getInputData(channelConfig), this.options.getInputUnitMeasure(),
                ExampleDriverChannelDescriptor.getOutputUnitMeasure(channelConfig), listener));

    }

    @Override
    public void unregisterChannelListener(ChannelListener listener) throws ConnectionException {

        this.channelListeners.removeIf(list -> list.getChannelListener().equals(listener));

    }

    @Override
    public void write(List<ChannelRecord> records) throws ConnectionException {

        for (ChannelRecord chRecord : records) {

            Double writingValue = MeasureConverter.convertFromDataType(chRecord.getValue(), chRecord.getValueType());
            Double result = MeasureConverter.convertMeasure(this.options.getInputUnitMeasure(), writingValue,
                    ExampleDriverChannelDescriptor.getOutputUnitMeasure(chRecord.getChannelConfig()));

            logger.info("Input value {} in {}, has a value of {} in {}", chRecord.getValue().getValue(),
                    this.options.getInputUnitMeasure(), result,
                    ExampleDriverChannelDescriptor.getOutputUnitMeasure(chRecord.getChannelConfig()));
        }

    }

    @Override
    public PreparedRead prepareRead(List<ChannelRecord> records) {
        return new ExampleDriverPrepareRead(records);
    }

    public class ExampleDriverPrepareRead implements PreparedRead {

        List<ChannelRecord> recordList;

        public ExampleDriverPrepareRead(List<ChannelRecord> records) {
            this.recordList = records;
        }

        @Override
        public void close() throws Exception {
            logger.info("Closing...");
        }

        @Override
        public List<ChannelRecord> execute() throws ConnectionException, KuraException {
            read(this.recordList);
            return this.recordList;
        }

        @Override
        public List<ChannelRecord> getChannelRecords() {
            return this.recordList;
        }

    }

}
