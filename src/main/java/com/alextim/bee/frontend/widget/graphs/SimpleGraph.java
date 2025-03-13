package com.alextim.bee.frontend.widget.graphs;


import com.alextim.bee.service.ValueFormatter;
import de.gsi.dataset.spi.DoubleDataSet;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

import static com.alextim.bee.context.Property.MEAS_DATA_NUMBER_SING_DIGITS;

public class SimpleGraph extends AbstractGraph {

    public DoubleDataSet scoresDataSet;

    public SimpleGraph(SimpleStringProperty title,
                       SimpleStringProperty progressTitle) {
        super(title, null, progressTitle, null, null, null);
        scoresDataSet = new DoubleDataSet(title.get());
    }

    public void addPoint(int index, long x, double y, String measValueTitle, String unit) {
        scoresDataSet.add(x, y);
        scoresDataSet.addDataLabel(index, title.get() + ". " + measValueTitle + ": " +
                new ValueFormatter(y, unit, MEAS_DATA_NUMBER_SING_DIGITS));
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
