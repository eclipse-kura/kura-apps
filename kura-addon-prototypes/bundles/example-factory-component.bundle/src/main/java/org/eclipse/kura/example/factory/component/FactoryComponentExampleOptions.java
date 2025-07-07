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

import java.util.Objects;

public class FactoryComponentExampleOptions {

    private final int tcpPort;
    private final String availableMessages;
    private final boolean caseSensitiveEnabled;

    public FactoryComponentExampleOptions(final FactoryComponentExampleOCD properties) {
        this.tcpPort = properties.tcp_port();
        this.availableMessages = properties.available_messages();
        this.caseSensitiveEnabled = properties.case_sensitive();
    }

    public int getTcpPort() {
        return this.tcpPort;
    }

    public String getAvailableMessages() {
        return this.availableMessages;
    }

    public boolean isCaseSensitiveEnabled() {
        return this.caseSensitiveEnabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(availableMessages, caseSensitiveEnabled, tcpPort);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FactoryComponentExampleOptions other = (FactoryComponentExampleOptions) obj;
        return Objects.equals(availableMessages, other.availableMessages)
                && caseSensitiveEnabled == other.caseSensitiveEnabled && tcpPort == other.tcpPort;
    }

}
