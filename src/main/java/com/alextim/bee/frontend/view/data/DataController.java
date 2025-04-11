package com.alextim.bee.frontend.view.data;

import com.alextim.bee.client.messages.DetectorCommands.SetMeasTimeCommand;
import com.alextim.bee.service.AccumulationMeasService.AccumulatedMeasurement;
import com.alextim.bee.service.StatisticMeasService.StatisticMeasurement;
import com.alextim.bee.service.ValueFormatter;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.time.ZoneId;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.alextim.bee.context.Property.*;
import static com.alextim.bee.service.ValueFormatter.sigDigRounder;


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
        currentMeasDataGraph.addPoint(index, timestamp, meas.currentMeasDataValue, meas.measDataTitle, meas.measDataUnit,
                meas.currentCount1, meas.currentCount2);
        averageMeasDataGraph.addPoint(index, timestamp, meas.averageMeasDataValue, meas.measDataTitle, meas.measDataUnit,
                meas.averageCount1, meas.averageCount2);
/*
        accumulatedMeasDataGraph.addPoint(index, timestamp, meas.accumulatedMeasDataValue, meas.measDataTitle, meas.measDataUnit,
                meas.averageCount1, meas.averageCount2);
        accumulatedPowerMeasDataGraph.addPoint(index, timestamp, meas.accumulatedPowerMeasDataValue, meas.measDataTitle,
                meas.measDataUnit, meas.averageCount1, meas.averageCount2);
*/

        index++;

        updateTable(meas);
        setCounts(meas);

        if (DETECTOR_APP.equals(MG_DETECTOR_APP)) {
            String formattedMeasData = new ValueFormatter(
                    Math.abs(meas.currentMeasDataValue), meas.measDataUnit, MEAS_DATA_NUMBER_SING_DIGITS)
                    .toString();

            setMeasData(meas.measDataTitle, (meas.currentMeasDataValue < 0 ? "-" : "") + formattedMeasData);

        } else if (DETECTOR_APP.equals(PN_DETECTOR_APP)) {
            setMeasData(meas.measDataTitle,
                    sigDigRounder(meas.currentMeasDataValue, MEAS_DATA_NUMBER_SING_DIGITS) + " " + meas.measDataUnit);
        }

        setMeasTime(meas.accInterval + " сек");

        setMode(meas.mode.title);

        setGeoData(String.format(Locale.US, "%f, %f", meas.geoData.lat(), meas.geoData.lon()));
    }

    public void showAccumulatedMeas(AccumulatedMeasurement accumulatedMeasurement) {
        Platform.runLater(() -> {
            if (DETECTOR_APP.equals(MG_DETECTOR_APP)) {
                String formattedMeasData = new ValueFormatter(
                        Math.abs(accumulatedMeasurement.aveMeasData), accumulatedMeasurement.unit, MEAS_DATA_NUMBER_SING_DIGITS)
                        .toString();

                setAccMeasData((accumulatedMeasurement.aveMeasData < 0 ? "-" : "") + formattedMeasData);

            } else if (DETECTOR_APP.equals(PN_DETECTOR_APP)) {
                setAccMeasData(
                        sigDigRounder(accumulatedMeasurement.aveMeasData,
                                MEAS_DATA_NUMBER_SING_DIGITS) + " " + accumulatedMeasurement.unit);


            }

            setProgress(accumulatedMeasurement.progress);
        });

    }

    @Override
    void start(long measTime) {
        index = 0;
        rootController.startMeasurement();
    }

    @Override
    void startAccumulate(int measAmount) {
        setProgress(0);
        rootController.startAccumulation(measAmount);
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
        clearTable();
/*
        accumulatedMeasDataGraph.clear();
        accumulatedPowerMeasDataGraph.clear();
*/
        rootController.clear();
    }

}
