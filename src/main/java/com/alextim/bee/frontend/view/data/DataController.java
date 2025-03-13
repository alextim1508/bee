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

        long timestamp = meas.localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        currentMeasDataGraph.addPoint(index, timestamp, meas.currentMeasDataValue);
        averageMeasDataGraph.addPoint(index, timestamp, meas.averageMeasDataValue);
        accumulatedMeasDataGraph.addPoint(index, timestamp, meas.accumulatedMeasDataValue);
        accumulatedPowerMeasDataGraph.addPoint(index, timestamp, meas.accumulatedPowerMeasDataValue);

        index++;

        updateTable(meas);
        setCounts(meas);

        String formattedMeasData = new ValueFormatter(
                Math.abs(meas.currentMeasDataValue), meas.measDataUnit, MEAS_DATA_NUMBER_SING_DIGITS)
                .toString();

        setMeasData(meas.measDataTitle, (meas.currentMeasDataValue < 0 ? "-" : "") + formattedMeasData);

        setMeasTime(meas.accInterval + " сек");

        setMode(meas.mode.title);

        setGeoData(String.format(Locale.US, "%f, %f", meas.geoData.lat(), meas.geoData.lon()));
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

        currentMeasDataGraph.clear();
        averageMeasDataGraph.clear();
        accumulatedMeasDataGraph.clear();
        accumulatedPowerMeasDataGraph.clear();

        rootController.clear();
    }
}
