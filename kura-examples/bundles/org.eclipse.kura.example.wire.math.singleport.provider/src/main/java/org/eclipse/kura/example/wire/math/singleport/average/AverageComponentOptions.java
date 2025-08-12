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

package org.eclipse.kura.example.wire.math.singleport.average;

import org.eclipse.kura.example.wire.math.singleport.MathSingleportProviderOptions;

public class AverageComponentOptions extends MathSingleportProviderOptions {

    public AverageComponentOptions(AverageComponentOCD ocd) {
        super(ocd.operand_name(), ocd.result_name(), ocd.window_size(), ocd.emit_received_properties());
    }

}
