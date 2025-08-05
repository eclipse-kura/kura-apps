package org.eclipse.kura.example.wire.math.singleport.minimum;

import org.eclipse.kura.example.wire.math.singleport.MathSingleportProviderOptions;

public class MinimumComponentOptions extends MathSingleportProviderOptions {

    public MinimumComponentOptions(MinimumComponentOCD ocd) {
        super(ocd.operand_name(), ocd.result_name(), ocd.window_size(), ocd.emit_received_properties());
    }
}
