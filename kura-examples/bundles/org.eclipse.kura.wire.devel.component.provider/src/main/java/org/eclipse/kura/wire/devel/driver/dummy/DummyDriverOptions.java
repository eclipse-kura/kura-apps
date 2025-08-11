/*******************************************************************************
 * Copyright (c) 2018, 2025 Eurotech and/or its affiliates and others
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
package org.eclipse.kura.wire.devel.driver.dummy;

public class DummyDriverOptions {

    private final Integer connectionDelay;
    private final String channelDescriptorIssues;
    private final String preparedReadIssues;
    private final String connectionIssues;

    public DummyDriverOptions(DummyDriverOCD ocd) {
        this.connectionDelay = ocd.connection_delay();
        this.channelDescriptorIssues = ocd.channel_descriptor_issues();
        this.preparedReadIssues = ocd.prepared_read_issues();
        this.connectionIssues = ocd.connection_issues();
    }

    public int getConnectionDelay() {
        return this.connectionDelay;
    }

    public ChannelDescriptorIssue getChannelDescriptorIssues() {
        try {
            return ChannelDescriptorIssue.valueOf(this.channelDescriptorIssues);
        } catch (Exception e) {
            return ChannelDescriptorIssue.NONE;
        }
    }

    public PreparedReadIssue getPreparedReadIssues() {
        try {
            return PreparedReadIssue.valueOf(this.preparedReadIssues);
        } catch (Exception e) {
            return PreparedReadIssue.NONE;
        }
    }

    public ConnectionIssue getConnectionIssues() {
        try {
            return ConnectionIssue.valueOf(this.connectionIssues);
        } catch (Exception e) {
            return ConnectionIssue.NONE;
        }
    }
}