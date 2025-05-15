package org.eclipse.kura.example.driver.conversion;

import org.eclipse.kura.example.driver.ExampleDriverChannelDescriptor;
import org.eclipse.kura.example.driver.ExampleDriverChannelDescriptor.UnitMeasure;
import org.eclipse.kura.type.DataType;
import org.eclipse.kura.type.TypedValue;
import org.eclipse.kura.type.TypedValues;

public class MeasureConverter {

    private static final String ERROR_MESSAGE_UNKNOWN_OUTPUT_UM = "Impossible to convert data: unknown output unit measure";
    private static final String ERROR_MESSAGE_UNKNOWN_INPUT_UM = "Impossible to convert data: unknown input unit measure";

    private static final Double METER_TO_YARD = 1.09361;
    private static final Double METER_TO_INCH = 39.36996;

    private static final Double INCH_TO_YARD = 0.0277778;
    private static final Double INCH_TO_METER = 0.0254;

    private static final Double YARD_TO_INCH = 36.0;
    private static final Double YARD_TO_METER = 0.9144;

    private MeasureConverter() {

    }

    public static Double convertMeasure(UnitMeasure inputUm, Double inputData, UnitMeasure outputUm)
            throws UnsupportedOperationException {

        if (inputUm.equals(outputUm)) {
            return inputData;
        }

        switch (inputUm) {
        case METER: {
            if (outputUm.equals(UnitMeasure.INCH)) {
                return inputData * METER_TO_INCH;
            } else if (outputUm.equals(UnitMeasure.YARD)) {
                return inputData * METER_TO_YARD;
            } else {
                throw new UnsupportedOperationException(ERROR_MESSAGE_UNKNOWN_OUTPUT_UM);
            }
        }

        case INCH: {
            if (outputUm.equals(UnitMeasure.METER)) {
                return inputData * INCH_TO_METER;
            } else if (outputUm.equals(UnitMeasure.YARD)) {
                return inputData * INCH_TO_YARD;
            } else {
                throw new UnsupportedOperationException(ERROR_MESSAGE_UNKNOWN_OUTPUT_UM);
            }
        }

        case YARD: {
            if (outputUm.equals(UnitMeasure.METER)) {
                return inputData * YARD_TO_METER;
            } else if (outputUm.equals(UnitMeasure.INCH)) {
                return inputData * YARD_TO_INCH;
            } else {
                throw new UnsupportedOperationException(ERROR_MESSAGE_UNKNOWN_OUTPUT_UM);
            }
        }

        default:
            throw new UnsupportedOperationException(ERROR_MESSAGE_UNKNOWN_INPUT_UM);
        }
    }

    public static TypedValue<?> convertToDataType(Double inputData, DataType dataType)
            throws UnsupportedOperationException {

        switch (dataType) {
        case INTEGER: {
            return TypedValues.newIntegerValue(inputData.intValue());
        }
        case LONG: {
            return TypedValues.newLongValue(inputData.longValue());
        }
        case FLOAT: {
            return TypedValues.newFloatValue(inputData.floatValue());
        }
        case DOUBLE: {
            return TypedValues.newDoubleValue(inputData);
        }

        default:
            throw new UnsupportedOperationException(
                    "Impossible to convert data: only available data type are Integer, Long, Float or Double");
        }
    }

    public static Double convertFromDataType(TypedValue<?> inputData, DataType dataType)
            throws UnsupportedOperationException {

        switch (dataType) {
        case INTEGER: {
            return ((Integer) inputData.getValue()).doubleValue();
        }
        case LONG: {
            return ((Long) inputData.getValue()).doubleValue();
        }
        case FLOAT: {
            return ((Float) inputData.getValue()).doubleValue();
        }
        case DOUBLE: {
            return ((Double) inputData.getValue());
        }

        default:
            throw new UnsupportedOperationException(
                    "Impossible to convert data: only available data type are Integer, Long, Float or Double");
        }
    }
}
