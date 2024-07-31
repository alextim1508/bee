package com.alextim.bee;


import com.alextim.bee.client.DetectorClientAbstract;
import com.alextim.bee.client.messages.DetectorEvents.AccumulationStateDetector;
import com.alextim.bee.client.messages.DetectorEvents.InitializationStateDetector;
import com.alextim.bee.client.messages.DetectorMsg;
import com.alextim.bee.context.AppState;
import com.alextim.bee.frontend.MainWindow;
import com.alextim.bee.frontend.view.data.DataController;
import com.alextim.bee.frontend.view.magazine.MagazineController;
import com.alextim.bee.service.ExportService;
import com.alextim.bee.service.StatisticMeasService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.alextim.bee.client.messages.DetectorEvents.InternalEvent;
import static com.alextim.bee.client.messages.DetectorEvents.MeasurementStateDetector;
import static com.alextim.bee.client.transfer.DetectorParser.parse;
import static com.alextim.bee.service.StatisticMeasService.StatisticMeasurement;

@Slf4j
public class RootController extends RootControllerInitializer {

    public RootController(AppState appState,
                          MainWindow mainWindow,
                          DetectorClientAbstract detectorClient,
                          StatisticMeasService statisticMeasService,
                          ExportService exportService) {
        super(appState, mainWindow, detectorClient, statisticMeasService, exportService);
    }

    private final List<DetectorMsg> detectorMsgs = new ArrayList<>();
    private final Map<Long, StatisticMeasurement> statisticMeasurements = new HashMap<>();

    private Future<?> queueHandlerTask;

    protected final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @SneakyThrows
    public void connect() {
        Runnable task = () -> {
            DataController dataController = (DataController) getChild(DataController.class.getSimpleName());
            MagazineController magazineController = (MagazineController) getChild(MagazineController.class.getSimpleName());

            detectorClient.connect();

            while (!Thread.currentThread().isInterrupted()) {
                DetectorMsg msg = null;
                try {
                    msg = detectorClient.waitAndGetDetectorMsg();
                } catch (InterruptedException e) {
                    log.error("Detector client Interrupted Exception");
                    return;
                }

                DetectorMsg detectorMsg = parse(msg);
                log.info("{} : {}", detectorMsg.getClass().getSimpleName(), detectorMsg);


                if (detectorMsg instanceof InitializationStateDetector initializationStateDetector) {
                    handleInitializationStateDetector(dataController, initializationStateDetector);

                } else if (detectorMsg instanceof AccumulationStateDetector accumulationStateDetector) {
                    handleAccumulationStateDetector(dataController, accumulationStateDetector);

                } else if (detectorMsg instanceof MeasurementStateDetector measStateDetector) {
                    handleMeasStateDetector(dataController, measStateDetector);

                } else if (detectorMsg instanceof InternalEvent internalEvent) {
                    handleInternalEvent(dataController, internalEvent);
                }

                magazineController.addLog(detectorMsg);

                detectorMsgs.add(detectorMsg);
            }
        };

        queueHandlerTask = executorService.submit(task);
    }

    private void handleInitializationStateDetector(DataController dataController, InitializationStateDetector initStateDetector) {
        dataController.setGrayCircle();
        dataController.setImageViewLabel("", "", "");
    }

    private void handleAccumulationStateDetector(DataController dataController, AccumulationStateDetector accStateDetector) {
        dataController.setYellowCircle();
        dataController.setImageViewLabel(100 * accStateDetector.curTime / accStateDetector.measTime + "%", null, null);
    }

    private void handleInternalEvent(DataController dataController, InternalEvent internalEvent) {
        dataController.setGreenCircle();
        dataController.setImageViewLabel("", "", "");

        if (statisticMeasurements.containsKey(internalEvent.internalData.measTime)) {
            StatisticMeasurement statMeas = statisticMeasurements.get(internalEvent.internalData.measTime);

            statisticMeasService.addMeasToStatistic(internalEvent.internalData, statMeas);
            statisticMeasService.initSumCounts(statMeas);

            dataController.showStatisticMeas(statMeas);
        } else {
            StatisticMeasurement statMeas = new StatisticMeasurement();
            statisticMeasService.addMeasToStatistic(internalEvent.internalData, statMeas);

            statisticMeasurements.put(internalEvent.internalData.measTime, statMeas);
        }
    }

    private void handleMeasStateDetector(DataController dataController, MeasurementStateDetector measStateDetector) {
        dataController.setGreenCircle();
        dataController.setImageViewLabel("", "", "");

        if (statisticMeasurements.containsKey(measStateDetector.meas.measTime)) {
            StatisticMeasurement statMeas = statisticMeasurements.get(measStateDetector.meas.measTime);

            statisticMeasService.addMeasToStatistic(measStateDetector.meas, statMeas);
            statisticMeasService.initSumCounts(statMeas);

            dataController.showStatisticMeas(statMeas);
        } else {
            StatisticMeasurement statMeas = new StatisticMeasurement();
            statisticMeasService.addMeasToStatistic(measStateDetector.meas, statMeas);

            statisticMeasurements.put(measStateDetector.meas.measTime, statMeas);
        }
    }


    public void startMeasurement(long measTime) {
        statisticMeasService.clearSumCounts();
    }

    public void setNewMeasTime(long seconds) {

    }

    public void stopMeasurement() {

    }

    public void saveMeasurements(File file) {
        exportService.exportMeasurements(statisticMeasurements.values(), file, (index, progress) -> {

        });
    }

    public void saveDetectorMessages(File file) {
        exportService.exportDetectorMsgs(detectorMsgs, file, (index, progress) -> {

        });
    }

    public void clear() {
        statisticMeasurements.clear();

        MagazineController magazineController = (MagazineController) getChild(MagazineController.class.getSimpleName());
        magazineController.clear();
    }

    public void close() {
        log.info("close");

        try {
            appState.saveParam();
        } catch (Exception e) {
            log.error("SaveParams error", e);
        }

        queueHandlerTask.cancel(true);

        executorService.shutdown();
    }
}
