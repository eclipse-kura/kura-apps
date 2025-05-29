package org.eclipse.kura.example.driver;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.eclipse.kura.configuration.metatype.Option;
import org.eclipse.kura.core.configuration.metatype.Tad;
import org.eclipse.kura.core.configuration.metatype.Toption;
import org.eclipse.kura.driver.ChannelDescriptor;
import org.eclipse.kura.example.driver.utils.AdProperty;

public class ExampleDriverChannelDescriptor implements ChannelDescriptor {

    public enum UnitMeasure {

        METER,
        YARD,
        INCH;

        @Override
        public String toString() {
            switch (this) {
            case METER: {
                return "meter";
            }
            case YARD: {
                return "yard";
            }
            case INCH: {
                return "inch";
            }
            default: {
                return "";
            }
            }
        }
    }

    private final Map<UnitMeasure, String> unitMeasureMap;

    private static final String INPUT_DATA_DESCRIPTION = "Input Data to be converted. Considered only in reading operations";
    private static final String OUTPUT_UNIT_MEASURE_DESCRIPTION = "Unit measure in which the input data will be converted";

    private static final String INPUT_DATA_ID = "input.data";
    private static final String OUTPUT_UNIT_MEASURE_ID = "output.unit.measure";

    private static final String INPUT_DATA_NAME = "Input Data (Reading Only)";
    private static final String OUTPUT_UNIT_MEASURE_NAME = "Output Unit Measure";

    private static final AdProperty<Double> INPUT_DATA_PROPERTY = AdProperty.required(INPUT_DATA_ID, INPUT_DATA_NAME,
            INPUT_DATA_DESCRIPTION, 0.0);

    private static final AdProperty<String> OUTPUT_UNIT_MEASURE_PROPERTY = AdProperty.required(OUTPUT_UNIT_MEASURE_ID,
            OUTPUT_UNIT_MEASURE_NAME, OUTPUT_UNIT_MEASURE_DESCRIPTION, UnitMeasure.METER.name());

    private static final ExampleDriverChannelDescriptor INSTANCE = new ExampleDriverChannelDescriptor();

    private final List<Tad> ads = new ArrayList<>();

    private ExampleDriverChannelDescriptor() {
        this.unitMeasureMap = new EnumMap<>(UnitMeasure.class);
        this.unitMeasureMap.put(UnitMeasure.METER, "meter");
        this.unitMeasureMap.put(UnitMeasure.YARD, "yard");
        this.unitMeasureMap.put(UnitMeasure.INCH, "inch");

        this.ads.add(INPUT_DATA_PROPERTY.getAd());
        addUnitMeasuresType(unitMeasureMap, OUTPUT_UNIT_MEASURE_PROPERTY.getAd());
        this.ads.add(OUTPUT_UNIT_MEASURE_PROPERTY.getAd());

    }

    private static void addUnitMeasuresType(final Map<UnitMeasure, String> variants, final Tad ad) {
        final List<Option> options = ad.getOption();

        for (Map.Entry<UnitMeasure, String> variant : variants.entrySet()) {
            final Toption opt = new Toption();
            opt.setLabel(variant.getValue());
            opt.setValue(variant.getKey().name());
            options.add(opt);
        }
    }

    public static Double getInputData(final Map<String, Object> properties) {
        return INPUT_DATA_PROPERTY.get(properties);
    }

    public static UnitMeasure getOutputUnitMeasure(final Map<String, Object> properties) {
        return UnitMeasure.valueOf(OUTPUT_UNIT_MEASURE_PROPERTY.get(properties));
    }

    public static ExampleDriverChannelDescriptor instance() {
        return INSTANCE;
    }

    @Override
    public Object getDescriptor() {
        return this.ads;
    }

}
