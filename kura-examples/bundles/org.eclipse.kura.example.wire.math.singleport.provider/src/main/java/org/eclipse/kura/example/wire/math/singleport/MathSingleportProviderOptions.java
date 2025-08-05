package org.eclipse.kura.example.wire.math.singleport;

public class MathSingleportProviderOptions {

    private final String operandName;
    private final String resultName;
    private final Integer windowSize;
    private final Boolean emitReceivedProperties;

    public MathSingleportProviderOptions(String operand, String result, Integer windSize, Boolean emitPorts) {
        this.operandName = operand;
        this.resultName = result;
        this.windowSize = windSize;
        this.emitReceivedProperties = emitPorts;
    }

    public String getOperandName() {
        return this.operandName;
    }

    public String getResultName() {
        return this.resultName;
    }

    public Integer getWindowSize() {
        return this.windowSize;
    }

    public Boolean shouldEmitReceivedProperties() {
        return this.emitReceivedProperties;
    }
}
