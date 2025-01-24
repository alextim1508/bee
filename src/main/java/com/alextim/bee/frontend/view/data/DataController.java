package com.alextim.bee.frontend.view.data;

import com.alextim.bee.client.messages.DetectorCommands.SetMeasTimeCommand;
import com.alextim.bee.service.StatisticMeasService.StatisticMeasurement;
import com.alextim.bee.service.ValueFormatter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.time.ZoneId;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.alextim.bee.context.Property.MEAS_DATA_NUMBER_SING_DIGITS;
import static com.alextim.bee.context.Property.TRANSFER_TO_DETECTOR_ID;


@Slf4j
public class DataController extends DataControllerInitializer {

    @Override
    protected String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        changeDisableStartStopBtn(false);
    }

    private int index;

    public void showStatisticMeas(StatisticMeasurement meas) {
        log.info("ShowStatisticMeas: {}", meas);

        long timestamp = meas.getLocalDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        countGraph.addPoint(index, timestamp, meas.getCountSum());
        averageCountGraph.addPoint(index, timestamp, meas.getAverageCountSum());
        currentCountGraph.addPoint(index, timestamp, meas.getCurrentCountSum());

        index++;

        updateTable(meas);

        String formattedMeasData = new ValueFormatter(
                Math.abs(meas.getCurrentMeasDataValue()), meas.getMeasDataUnit(), MEAS_DATA_NUMBER_SING_DIGITS)
                .toString();

        setMeasData(meas.getMeasDataTitle(), (meas.getCurrentMeasDataValue() < 0 ? "-" : "") + formattedMeasData);

        setMeasTime(meas.getAccInterval() + " сек");

        setGeoData(String.format(Locale.US, "%f, %f", meas.getGeoData().lat(), meas.getGeoData().lon()));
    }

    @Override
    void start(long measTime) {
        index = 0;
        rootController.startMeasurement();
    }

    @Override
    void stop() {
        rootController.stopMeasurement();
    }

    @Override
    void save() {
        File file = mainWindow.showFileChooseDialog();
        if (file != null) {
            rootController.saveMeasurements(file, getFileComment());
        }
    }

    @Override
    void setNewMeasTime(MeasTime measTime) {
        rootController.sendDetectorCommand(new SetMeasTimeCommand(TRANSFER_TO_DETECTOR_ID, measTime.seconds));
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
