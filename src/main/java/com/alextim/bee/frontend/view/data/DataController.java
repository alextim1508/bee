package com.alextim.bee.frontend.view.data;

import com.alextim.bee.service.ValueFormatter;
import com.alextim.bee.service.StatisticMeasService.StatisticMeasurement;
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

        changeDisableStartStopBtn(true);
    }

    private int index;

    public void showStatisticMeas(StatisticMeasurement meas) {
        log.info("ShowStatisticMeas: {}", meas);

        long timestamp = meas.getTimestamp();
        countGraph.addPoint(index, timestamp, meas.getCountSum());
        averageCountGraph.addPoint(index, timestamp, meas.getAverageCountSum());
        currentCountGraph.addPoint(index, timestamp, meas.getCurrentCountSum());

        index++;

        updateTable(meas);

        setMeasData(meas.getMeasDataTitle(),
                new ValueFormatter(meas.getMeasDataValue(), meas.getMeasDataUnit()).toString());

        setMeasTime(meas.getMeasTime() + " сек");

        setGeoData("59.9386, 30.3141");
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

    @Override
    void setNewMeasTime(MeasTime measTime) {
        rootController.setNewMeasTime(measTime.seconds);
    }

    @Override
    void clear() {
        index = 0;

        countGraph.clear();
        averageCountGraph.clear();
        currentCountGraph.clear();

        rootController.clear();
    }
}
