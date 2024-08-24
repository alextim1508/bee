package com.alextim.bee.frontend.widget.graphs;


import de.gsi.dataset.spi.DoubleDataSet;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.alextim.bee.context.Property.DATE_FORMAT;

public class SimpleGraph extends AbstractGraph {

    public DoubleDataSet scoresDataSet;

    public SimpleGraph(SimpleStringProperty title,
                       SimpleStringProperty progressTitle) {
        super(title, null, progressTitle, null, null, null);
        scoresDataSet = new DoubleDataSet(title.get());
    }


    private final String promptFormat = "%s Номер: %d Счет: %.1f Время: %s";

    public void addPoint(int index, long x, double y) {
        scoresDataSet.add(x, y);
        scoresDataSet.addDataLabel(index, String.format(promptFormat, title.get(), index, y, DATE_FORMAT.format(new Date(x))));
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
