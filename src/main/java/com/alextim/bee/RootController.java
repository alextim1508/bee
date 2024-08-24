package com.alextim.bee;


import com.alextim.bee.client.DetectorClientAbstract;
import com.alextim.bee.client.messages.DetectorCommands.*;
import com.alextim.bee.client.messages.DetectorEvents.AccumulationDetectorState;
import com.alextim.bee.client.messages.DetectorEvents.InitializationDetectorState;
import com.alextim.bee.client.messages.DetectorEvents.RestartDetector;
import com.alextim.bee.client.messages.DetectorEvents.SomeEvent;
import com.alextim.bee.client.messages.DetectorMsg;
import com.alextim.bee.context.AppState;
import com.alextim.bee.frontend.MainWindow;
import com.alextim.bee.frontend.dialog.progress.ProgressDialog;
import com.alextim.bee.frontend.view.data.DataController;
import com.alextim.bee.frontend.view.magazine.MagazineController;
import com.alextim.bee.frontend.view.management.ManagementController;
import com.alextim.bee.service.ExportService;
import com.alextim.bee.service.StatisticMeasService;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
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
import static com.alextim.bee.client.messages.DetectorEvents.MeasurementDetectorState;
import static com.alextim.bee.client.protocol.DetectorCodes.BDParam.*;
import static com.alextim.bee.client.protocol.DetectorCodes.CommandStatus.SUCCESS;
import static com.alextim.bee.client.protocol.DetectorCodes.Error.getErrorByCode;
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

    protected final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @SneakyThrows
    public void connect() {
        Runnable task = () -> {
            DataController dataController = (DataController) getChild(DataController.class.getSimpleName());
            MagazineController magazineController = (MagazineController) getChild(MagazineController.class.getSimpleName());
            ManagementController managementController = (ManagementController) getChild(ManagementController.class.getSimpleName());

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
                log.info("DetectorMsg: {}. {}", detectorMsg.getClass().getSimpleName(), detectorMsg);

                if (detectorMsg instanceof SomeEvent event) {
                    handleEvent(dataController, managementController, event);

                } else if (detectorMsg instanceof SomeCommandAnswer answer) {
                    handleCommandAnswer(managementController, answer);
                }

                magazineController.addLog(detectorMsg);

                detectorMsgs.add(detectorMsg);
            }
        };

        queueHandlerTask = executorService.submit(task);
    }

    private void handleEvent(DataController dataController, ManagementController managementController, SomeEvent detectorMsg) {
        if (detectorMsg instanceof RestartDetector restartDetector) {
            handleRestartDetector(managementController, restartDetector);

        } else if (detectorMsg instanceof InitializationDetectorState state) {
            handleInitializationDetectorState(dataController, state);

        } else if (detectorMsg instanceof AccumulationDetectorState state) {
            handleAccumulationDetectorState(dataController, state);

        } else if (detectorMsg instanceof MeasurementDetectorState state) {
            handleMeasDetectorState(dataController, state);

        } else if (detectorMsg instanceof InternalEvent event) {
            handleInternalEvent(dataController, event);
        }
    }

    private void handleRestartDetector(ManagementController managementController, RestartDetector restartDetector) {
        managementController.setIpInfo(restartDetector.ipAddr, restartDetector.ipPort, restartDetector.externalDeviceIpPort);
        managementController.showDialogDetectorIsRestarted();
    }

    private void handleInitializationDetectorState(DataController dataController, InitializationDetectorState initStateDetector) {
        dataController.setGrayCircle();
        dataController.setImageViewLabel("", "", "");
    }

    private void handleAccumulationDetectorState(DataController dataController, AccumulationDetectorState accStateDetector) {
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

    private void handleMeasDetectorState(DataController dataController, MeasurementDetectorState measStateDetector) {
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

    private void handleCommandAnswer(ManagementController managementController, SomeCommandAnswer detectorMsg) {
        if (detectorMsg instanceof SetMeasTimeAnswer answer) {
            if (detectorMsg.commandStatusCode == SUCCESS) {
                managementController.showDialogParamIsSet(MEAS_TIME);
            } else {
                managementController.showAnswerErrorDialog(MEAS_TIME, getErrorByCode(detectorMsg.data[0]));
            }

        } else if (detectorMsg instanceof SetSensitivityAnswer answer) {
            if (detectorMsg.commandStatusCode == SUCCESS) {
                managementController.showDialogParamIsSet(SENSITIVITY);
            } else {
                managementController.showAnswerErrorDialog(SENSITIVITY, getErrorByCode(detectorMsg.data[0]));
            }

        } else if (detectorMsg instanceof GetSensitivityAnswer answer) {
            if (detectorMsg.commandStatusCode == SUCCESS) {
                managementController.setSensitivity(answer.sensitivity);
                managementController.showDialogParamIsGot(SENSITIVITY);
            } else {
                managementController.showAnswerErrorDialog(SENSITIVITY, getErrorByCode(detectorMsg.data[0]));
            }

        } else if (detectorMsg instanceof SetDeadTimeAnswer answer) {
            if (detectorMsg.commandStatusCode == SUCCESS) {
                managementController.showDialogParamIsSet(DEAD_TIME);
            } else {
                managementController.showAnswerErrorDialog(DEAD_TIME, getErrorByCode(detectorMsg.data[0]));
            }

        } else if (detectorMsg instanceof GetDeadTimeAnswer answer) {
            if (detectorMsg.commandStatusCode == SUCCESS) {
                managementController.setDeadTime(answer.deadTime);
                managementController.showDialogParamIsGot(DEAD_TIME);
            } else {
                managementController.showAnswerErrorDialog(DEAD_TIME, getErrorByCode(detectorMsg.data[0]));
            }

        } else if (detectorMsg instanceof SetCounterCorrectCoeffAnswer answer) {
            if (detectorMsg.commandStatusCode == SUCCESS) {
                managementController.showDialogParamIsSet(COR_COEF);
            } else {
                managementController.showAnswerErrorDialog(COR_COEF, getErrorByCode(detectorMsg.data[0]));
            }

        } else if (detectorMsg instanceof GetCounterCorrectCoeffAnswer answer) {
            if (detectorMsg.commandStatusCode == SUCCESS) {
                managementController.setCounterCorrectCoeff(answer.counterIndex, answer.counterCorrectCoeff);
                managementController.showDialogParamIsGot(COR_COEF);
            } else {
                managementController.showAnswerErrorDialog(COR_COEF, getErrorByCode(detectorMsg.data[0]));
            }

        } else if (detectorMsg instanceof GetVersionAnswer answer) {
            if (detectorMsg.commandStatusCode == SUCCESS) {
                managementController.setHardwareVersion(answer.version);
                managementController.showDialogParamIsSet(VER_HARDWARE);
            } else {
                managementController.showAnswerErrorDialog(VER_HARDWARE, getErrorByCode(detectorMsg.data[0]));
            }
        }
    }

    public void startMeasurement(long measTime) {
        statisticMeasService.clearSumCounts();

    }

    public void stopMeasurement() {

    }

    public void sendDetectorCommand(SomeCommand command) {
        log.info("=========== SEND DATA ===========");

        StringBuilder str = new StringBuilder();
        for (int i = 0; i < command.data.length; i++)
            str.append(String.format("%x ", command.data[i]));
        log.info(str.toString());

        ((MagazineController) getChild(MagazineController.class.getSimpleName())).addLog(command);

        detectorClient.sendCommand(command);
    }

    public void saveMeasurements(File file) {
        DoubleProperty progressProperty = new SimpleDoubleProperty(0.0);
        StringProperty statusProperty = new SimpleStringProperty("");

        final ProgressDialog progressDialog = mainWindow.showProgressDialog(progressProperty, statusProperty);

        executorService.submit(() -> {
            log.info("export to selected file {}", file);

            exportService.exportMeasurements(statisticMeasurements.values(), file, (n, progress) ->
                Platform.runLater(() -> {
                    progressProperty.set(progress);
                    statusProperty.set("Экспорт измерения " + n);
                })
            );

            Platform.runLater(() -> {
                progressDialog.forcefullyHideDialog();

                mainWindow.showDialog(Alert.AlertType.INFORMATION,
                        "Экспорт",
                        "Измерение",
                        "Измерения экспортированы в файлы");
            });
        });


    }

    public void saveDetectorMessages(File file) {
        DoubleProperty progressProperty = new SimpleDoubleProperty(0.0);
        StringProperty statusProperty = new SimpleStringProperty("");

        final ProgressDialog progressDialog = mainWindow.showProgressDialog(progressProperty, statusProperty);

        log.info("export to selected file {}", file);

        exportService.exportDetectorMsgs(detectorMsgs, file, (n, progress) ->
                Platform.runLater(() -> {
                    progressProperty.set(progress);
                    statusProperty.set("Экспорт измерения " + n);
                })
        );

        Platform.runLater(() -> {
            progressDialog.forcefullyHideDialog();

            mainWindow.showDialog(Alert.AlertType.INFORMATION,
                    "Экспорт",
                    "Измерение",
                    "Измерения экспортированы в файлы");
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

        detectorClient.shutdown();
    }
}
