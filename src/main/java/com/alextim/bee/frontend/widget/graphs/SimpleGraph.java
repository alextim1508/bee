package com.alextim.bee.frontend.widget.graphs;


import com.alextim.bee.service.ValueFormatter;
import de.gsi.dataset.spi.DoubleDataSet;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

import static com.alextim.bee.context.Property.*;
import static com.alextim.bee.service.ValueFormatter.sigDigRounder;

public class SimpleGraph extends AbstractGraph {

    public DoubleDataSet scoresDataSet;

    public SimpleGraph(SimpleStringProperty title,
                       SimpleStringProperty progressTitle) {
        super(title, null, progressTitle, null, null, null);
        scoresDataSet = new DoubleDataSet(title.get());
    }

    private final String format = "%s. %s %s. Счета: " + COUNTER_NUMBER_FORMAT + " " + COUNTER_NUMBER_FORMAT;

    public void addPoint(int index, long x, double y, String measValueTitle, String unit, float c1, float c2) {
        scoresDataSet.add(x, y);

        if (DETECTOR_APP.equals(MG_DETECTOR_APP)) {
            String label = String.format(format,
                    title.get(),
                    measValueTitle,
                    new ValueFormatter(y, unit, MEAS_DATA_NUMBER_SING_DIGITS),
                    c1, c2);

            scoresDataSet.addDataLabel(index, label);

        } else if (DETECTOR_APP.equals(PN_DETECTOR_APP)) {
            String label = String.format(format,
                    title.get(),
                    measValueTitle,
                    sigDigRounder(y, MEAS_DATA_NUMBER_SING_DIGITS) + " " + unit,
                    c1, c2);

            scoresDataSet.addDataLabel(index,  label);
        }

    }

    public void clear() {
        scoresDataSet.clearData();
    }

    @Override
    public List<DoubleDataSet> getDataSetList() {
        List<DoubleDataSet> list = new ArrayList<>();
        list.add(scoresDataSet);
        return list;
    }

    @Override
    public void reset() {
    }

    @Override
    public int size() {
        return 0;
    }
}
