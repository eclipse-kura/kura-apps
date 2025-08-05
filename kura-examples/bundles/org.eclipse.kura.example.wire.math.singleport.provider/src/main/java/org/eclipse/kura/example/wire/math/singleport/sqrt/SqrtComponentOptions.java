package org.eclipse.kura.example.wire.math.singleport.sqrt;

import org.eclipse.kura.example.wire.math.singleport.MathSingleportProviderOptions;

public class SqrtComponentOptions extends MathSingleportProviderOptions {

    public SqrtComponentOptions(SqrtComponentOCD ocd) {
        super(ocd.operand_name(), ocd.result_name(), null, ocd.emit_received_properties());
    }
}