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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.kura.channel.ChannelFlag;
import org.eclipse.kura.channel.ChannelRecord;
import org.eclipse.kura.channel.ChannelStatus;
import org.eclipse.kura.channel.listener.ChannelEvent;
import org.eclipse.kura.channel.listener.ChannelListener;
import org.eclipse.kura.example.driver.ExampleDriverChannelDescriptor.UnitMeasure;
import org.eclipse.kura.example.driver.conversion.MeasureConverter;
import org.eclipse.kura.type.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleDriverListener {

    private static final Logger logger = LoggerFactory.getLogger(ExampleDriverListener.class);

    private final String channelName;
    private final DataType dataType;
    private final Double channelInputData;
    private final UnitMeasure outputUnitMeasure;
    private final UnitMeasure inputUnitMeasure;

    private final ChannelListener channelListener;

    public ExampleDriverListener(String chName, DataType type, Double inputData, UnitMeasure inputUm,
            UnitMeasure outputUm, ChannelListener listener) {

        this.channelName = chName;
        this.dataType = type;

        this.channelInputData = inputData;
        this.outputUnitMeasure = outputUm;
        this.inputUnitMeasure = inputUm;

        this.channelListener = listener;
    }

    public void channelEvent() {
        ChannelRecord chRecord = null;

        try {

            chRecord = ChannelRecord.createReadRecord(this.channelName, dataType);

            Map<String, Object> chConfig = new HashMap<>();
            chConfig.put("+type", "READ");
            chRecord.setChannelConfig(chConfig);

            chRecord.setValue(MeasureConverter.convertToDataType(
                    MeasureConverter.convertMeasure(inputUnitMeasure, channelInputData, outputUnitMeasure), dataType));
            chRecord.setChannelStatus(new ChannelStatus(ChannelFlag.SUCCESS));
            chRecord.setTimestamp(System.currentTimeMillis());

            this.channelListener.onChannelEvent(new ChannelEvent(chRecord));

        } catch (Exception ex) {

            if (chRecord != null) {
                logger.warn("Failed to create channelEvent for {} channel", chRecord.getChannelName());
            } else {
                logger.warn("Failed to create channelEvent");
            }

        }
    }

    public ChannelListener getChannelListener() {
        return this.channelListener;
    }

}
