/*******************************************************************************
 * Copyright (c) 2017, 2025 Eurotech and/or its affiliates and others
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
package org.eclipse.kura.example.eddystone.scanner;

public class EddystoneScannerOptions {

    private final boolean enableScanning;
    private final String adapterName;
    private final int publishPeriod;
    private final int scanDuration;

    public EddystoneScannerOptions(EddystoneScannerOCD ocd) {
        this.enableScanning = ocd.enable_scanning();
        this.adapterName = ocd.iname();
        this.publishPeriod = ocd.publish_period();
        this.scanDuration = ocd.scan_duration();
    }

    public boolean isEnabled() {
        return this.enableScanning;
    }

    public String getAdapterName() {
        return this.adapterName;
    }

    public int getPublishPeriod() {
        return this.publishPeriod;
    }

    public int getScanDuration() {
        return this.scanDuration;
    }
}
