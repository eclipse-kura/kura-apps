package org.eclipse.kura.example.wire.math.singleport.variance;

import org.eclipse.kura.example.wire.math.singleport.MathSingleportProviderOptions;

public class VarianceComponentOptions extends MathSingleportProviderOptions {

    public VarianceComponentOptions(VarianceComponentOCD ocd) {
        super(ocd.operand_name(), ocd.result_name(), ocd.window_size(), ocd.emit_received_properties());
    }
}
