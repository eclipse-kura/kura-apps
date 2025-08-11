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

package org.eclipse.kura.example.wire.math.singleport.gainoffset;

import java.util.List;

public class GainOffsetComponentOptions {

    private List<GainOffsetEntry> entries;
    private boolean emitReceivedProperties;

    public GainOffsetComponentOptions(GainOffsetComponentOCD ocd) {
        this.emitReceivedProperties = ocd.emit_received_properties();
        this.entries = GainOffsetEntry.parseAll(ocd.configuration());
    }

    public List<GainOffsetEntry> getEntries() {
        return entries;
    }

    public boolean shouldEmitReceivedProperties() {
        return emitReceivedProperties;
    }
}
