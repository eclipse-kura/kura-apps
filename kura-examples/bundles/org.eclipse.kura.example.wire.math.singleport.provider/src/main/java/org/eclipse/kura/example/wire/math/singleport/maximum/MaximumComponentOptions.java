package org.eclipse.kura.example.wire.math.singleport.maximum;

import org.eclipse.kura.example.wire.math.singleport.MathSingleportProviderOptions;

public class MaximumComponentOptions extends MathSingleportProviderOptions {

    public MaximumComponentOptions(MaximumComponentOCD ocd) {
        super(ocd.operand_name(), ocd.result_name(), ocd.window_size(), ocd.emit_received_properties());
    }
}
