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

package org.eclipse.kura.example.wire.math.multiport;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.kura.example.wire.math.multiport.difference.DifferenceComponentOCD;
import org.eclipse.kura.example.wire.math.multiport.division.DivisionComponentOCD;
import org.eclipse.kura.example.wire.math.multiport.multiplication.MultiplicationComponentOCD;
import org.eclipse.kura.example.wire.math.multiport.sum.SumComponentOCD;

public class ParserOCD {

    private static final String FIRST_OPERAND_NAME_PROP_NAME = "operand.name.1";
    private static final String SECOND_OPERAND_NAME_PROP_NAME = "operand.name.2";
    private static final String RESULT_NAME_PROP_NAME = "result.name";
    private static final String BARRIER_MODALITY_PROPERTY_KEY = "barrier";

    private final String firstOperandName;
    private final String secondOperandName;
    private final String resultName;
    private final boolean useBarrier;

    public ParserOCD(DifferenceComponentOCD ocd) {
        this.firstOperandName = ocd.operand_name_1();
        this.secondOperandName = ocd.operand_name_2();
        this.resultName = ocd.result_name();
        this.useBarrier = ocd.barrier();
    }

    public ParserOCD(DivisionComponentOCD ocd) {
        this.firstOperandName = ocd.operand_name_1();
        this.secondOperandName = ocd.operand_name_2();
        this.resultName = ocd.result_name();
        this.useBarrier = ocd.barrier();
    }

    public ParserOCD(MultiplicationComponentOCD ocd) {
        this.firstOperandName = ocd.operand_name_1();
        this.secondOperandName = ocd.operand_name_2();
        this.resultName = ocd.result_name();
        this.useBarrier = ocd.barrier();
    }

    public ParserOCD(SumComponentOCD ocd) {
        this.firstOperandName = ocd.operand_name_1();
        this.secondOperandName = ocd.operand_name_2();
        this.resultName = ocd.result_name();
        this.useBarrier = ocd.barrier();
    }

    public Map<String, Object> toPropertiesMap() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(FIRST_OPERAND_NAME_PROP_NAME, this.firstOperandName);
        properties.put(SECOND_OPERAND_NAME_PROP_NAME, this.secondOperandName);
        properties.put(RESULT_NAME_PROP_NAME, this.resultName);
        properties.put(BARRIER_MODALITY_PROPERTY_KEY, this.useBarrier);
        return properties;
    }

}
