package com.alextim.bee.frontend.view.metrology;

import com.alextim.bee.service.MetrologyMeasService.MetrologyMeasurement;
import javafx.application.Platform;

public class MetrologyController extends MetrologyControllerInitializer{

    @Override
    protected void startMetrology(int cycleAmount, int measAmount, float realMeasData) {
        rootController.startMetrology(cycleAmount, measAmount, realMeasData);
    }

    public void showMetrologyMeas(MetrologyMeasurement metrologyMeas) {
        Platform.runLater(() -> {
            setAveMeasData(metrologyMeas.aveMeasData, metrologyMeas.unit);
            updateTable(metrologyMeas.cycle, metrologyMeas.measData, metrologyMeas.unit);
            setError(metrologyMeas.error);
            setProgress(metrologyMeas.progress);
        });
    }
}
