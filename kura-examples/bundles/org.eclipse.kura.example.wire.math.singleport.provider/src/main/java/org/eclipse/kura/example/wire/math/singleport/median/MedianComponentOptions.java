package org.eclipse.kura.example.wire.math.singleport.median;

import org.eclipse.kura.example.wire.math.singleport.MathSingleportProviderOptions;

public class MedianComponentOptions extends MathSingleportProviderOptions {

    public MedianComponentOptions(MedianComponentOCD ocd) {
        super(ocd.operand_name(), ocd.result_name(), ocd.window_size(), ocd.emit_received_properties());
    }
}
