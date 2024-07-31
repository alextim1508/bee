package com.alextim.bee.frontend.view.data;

import com.alextim.bee.service.FormattedValue;
import com.alextim.bee.service.StatisticMeasService.StatisticMeasurement;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


@Slf4j
public class DataController extends DataControllerInitializer {

    @Override
    protected String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);


        Platform.runLater(() -> {
            setState("Старт");
            setGreenCircle();
        });
    }

    private int index;

    public void showStatisticMeas(StatisticMeasurement meas) {
        log.info("ShowStatisticMeas: {}", meas);

        countGraph.addPoint(index, meas.timestamp, StatisticMeasurement.countSum);
        averageCountGraph.addPoint(index, meas.timestamp, meas.averageCountSum);
        currentCountGraph.addPoint(index, meas.timestamp, meas.currentCountSum);

        index++;

        updateTable(meas);

        Platform.runLater(() -> {
            setState("Старт");

            setMeasDataTitle(meas.measDataTitle);
            setGreenCircle();

            setMeasDataValue(new FormattedValue(meas.coef * meas.measDataValue, meas.measDataUnit, 3)
                    .toString());

            setProgress(1.0 * meas.measTime / getMeasTime());
            setMeasTime(meas.measTime + " сек");
        });
    }

    @Override
    void start(long measTime) {
        index = 0;
        rootController.startMeasurement(measTime);
    }

    @Override
    void stop() {
        rootController.stopMeasurement();
    }

    @Override
    void save() {
        File file = mainWindow.showFileChooseDialog();
        if (file != null) {
            rootController.saveMeasurements(file);
        }
    }
}
