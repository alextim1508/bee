package com.alextim.bee.frontend.widget.graphs;


import de.gsi.dataset.spi.DoubleDataSet;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public abstract class AbstractGraph {

    protected final SimpleStringProperty title;
    protected final SimpleDoubleProperty progress;
    protected final SimpleStringProperty progressTitle;
    protected final SimpleStringProperty addInformation;
    protected final float[] rowEnergy;
    protected final float[] rowScores;

    protected String color = "#000000";
    protected boolean show = true;
    protected boolean scale = true;

    public abstract List<DoubleDataSet> getDataSetList();
    public abstract void reset();

    public abstract int size();
}
