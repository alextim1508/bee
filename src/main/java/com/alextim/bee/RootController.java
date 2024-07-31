package com.alextim.bee;


import com.alextim.bee.client.DetectorClientAbstract;
import com.alextim.bee.client.dto.InternalData;
import com.alextim.bee.client.dto.Measurement;
import com.alextim.bee.client.messages.DetectorMsg;
import com.alextim.bee.frontend.MainWindow;
import com.alextim.bee.frontend.view.data.DataController;
import com.alextim.bee.frontend.view.magazine.MagazineController;
import com.alextim.bee.service.ExportService;
import com.alextim.bee.service.StatisticMeasService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;

import static com.alextim.bee.client.messages.DetectorEvents.*;
import static com.alextim.bee.client.transfer.DetectorParser.parse;
import static com.alextim.bee.service.StatisticMeasService.StatisticMeasurement;

@Slf4j
public class RootController extends RootControllerInitializer {

    public RootController(MainWindow mainWindow,
                          DetectorClientAbstract detectorClient,
                          StatisticMeasService statisticMeasService,
                          ExportService exportService) {
        super(mainWindow, detectorClient, statisticMeasService, exportService);
    }

    private final List<Measurement> measurements = new ArrayList<>();
    private final List<InternalData> internalData = new ArrayList<>();
    private Map<Long, StatisticMeasurement> statisticMeasurements = new HashMap<>();


    @SneakyThrows
    public void connect() {
        Thread thread = new Thread(() -> {

            DataController dataController = (DataController) getChild(DataController.class.getSimpleName());
            MagazineController magazineController = (MagazineController) getChild(MagazineController.class.getSimpleName());

            detectorClient.start();

            while (!Thread.currentThread().isInterrupted()) {
                DetectorMsg msg = null;
                try {
                    msg = detectorClient.getQueue().take();
                } catch (InterruptedException e) {
                    log.error("", e);
                    return;
                }

                DetectorMsg detectorMsg = parse(msg);
                log.info("Detector msg: {}", detectorMsg);


                if (detectorMsg instanceof MeasurementStateDetector measStateDetector) {
                    measurements.add(measStateDetector.meas);

                    if (statisticMeasurements.containsKey(measStateDetector.meas.measTime)) {
                        StatisticMeasurement statisticMeas = statisticMeasurements.get(measStateDetector.meas.measTime);

                        statisticMeasService.addMeasToStatistic(measStateDetector.meas, statisticMeas);

                        dataController.showStatisticMeas(statisticMeas);

                        statisticMeasurements.remove(measStateDetector.meas.measTime);
                    } else {
                        StatisticMeasurement statisticMeas = new StatisticMeasurement();
                        statisticMeasService.addMeasToStatistic(measStateDetector.meas, statisticMeas);
                        statisticMeasurements.put(measStateDetector.meas.measTime, statisticMeas);
                    }


                } else if (detectorMsg instanceof InternalEvent internalEvent) {
                    internalData.add(internalEvent.internalData);

                    if (statisticMeasurements.containsKey(internalEvent.internalData.measTime)) {
                        StatisticMeasurement statisticMeas = statisticMeasurements.get(internalEvent.internalData.measTime);

                        statisticMeasService.addMeasToStatistic(internalEvent.internalData, statisticMeas);

                        dataController.showStatisticMeas(statisticMeas);

                        statisticMeasurements.remove(internalEvent.internalData.measTime);
                    } else {
                        StatisticMeasurement statisticMeas = new StatisticMeasurement();
                        statisticMeasService.addMeasToStatistic(internalEvent.internalData, statisticMeas);
                        statisticMeasurements.put(internalEvent.internalData.measTime, statisticMeas);
                    }
                }

                magazineController.addLog(detectorMsg);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void startMeasurement(long measTime) {

    }

    public void stopMeasurement() {

    }

    public void saveMeasurements(File file) {

    }

    public void close() {
        log.info("close");
    }
}
