package com.alextim.bee;


import com.alextim.bee.client.DetectorClientAbstract;
import com.alextim.bee.client.messages.DetectorCommands.*;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.alextim.bee.client.messages.DetectorEvents.*;
import static com.alextim.bee.client.protocol.DetectorCodes.BDParam.*;
import static com.alextim.bee.client.protocol.DetectorCodes.CommandStatus.SUCCESS;
import static com.alextim.bee.client.protocol.DetectorCodes.Error.getErrorByCode;
import static com.alextim.bee.client.protocol.DetectorCodes.RestartReason.RESTART_COMMAND;
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

    protected final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private Future<?> connectTimer;
    private final AtomicLong time = new AtomicLong();

    @SneakyThrows
    public void listenDetectorClient() {
        Runnable task = () -> {

            DataController dataController = (DataController) getChild(DataController.class.getSimpleName());
            MagazineController magazineController = (MagazineController) getChild(MagazineController.class.getSimpleName());
            ManagementController managementController = (ManagementController) getChild(ManagementController.class.getSimpleName());

            while (!Thread.currentThread().isInterrupted()) {
                DetectorMsg msg;
                try {
                    msg = detectorClient.waitAndGetDetectorMsg();
                } catch (InterruptedException e) {
                    log.error("Detector client Interrupted Exception");
                    break;
                }

                time.set(System.currentTimeMillis());

                DetectorMsg detectorMsg = parse(msg);
                log.info("DetectorMsg: {}. {}", detectorMsg.getClass().getSimpleName(), detectorMsg);

                if (detectorMsg instanceof SomeEvent event) {
                    try {
                        handleEvent(dataController, managementController, event);
                    } catch (Exception e) {
                        log.error("handleEvent exception", e);
                    }

                } else if (detectorMsg instanceof SomeCommandAnswer answer) {
                    try {
                        handleCommandAnswer(managementController, answer);
                    } catch (Exception e) {
                        log.error("handleCommandAnswer exception", e);
                    }
                }

                magazineController.addLog(detectorMsg);

                detectorMsgs.add(detectorMsg);
            }
            log.info("queue handle task is done");
        };

        executorService.submit(task);
    }

    private void handleEvent(DataController dataController, ManagementController managementController, SomeEvent detectorMsg) {
        if (detectorMsg instanceof RestartDetectorState restartDetectorState) {
            handleRestartDetectorState(managementController, restartDetectorState);

        } else if (detectorMsg instanceof InitializationDetectorState state) {
            handleInitializationDetectorState(dataController, state);

        } else if (detectorMsg instanceof AccumulationDetectorState state) {
            handleAccumulationDetectorState(dataController, state);

        } else if (detectorMsg instanceof MeasurementDetectorState state) {
            handleMeasDetectorState(dataController, state);

        } else if (detectorMsg instanceof InternalEvent event) {
            handleInternalEvent(dataController, event);

        } else if (detectorMsg instanceof ErrorDetectorState errorDetectorState) {
            handleErrorDetectorState(dataController, errorDetectorState);
        }
    }

    private void handleErrorDetectorState(DataController dataController, ErrorDetectorState errorDetectorState) {
        dataController.setRedCircle();
        dataController.setImageViewLabel(String.format("0x%x", errorDetectorState.error.code), "", "");
    }

    private void handleRestartDetectorState(ManagementController managementController, RestartDetectorState restartDetectorState) {
        managementController.setIpInfo(
                restartDetectorState.detectorIpAddr,
                restartDetectorState.ipPort,
                restartDetectorState.externalDeviceIpPort);

        if (restartDetectorState.reason == RESTART_COMMAND) {
            managementController.showDialogDetectorIsNormallyRestarted();
        } else {
            managementController.showDialogDetectorIsCrashRestarted(restartDetectorState.reason.title);
        }
    }

    private void handleInitializationDetectorState(DataController dataController, InitializationDetectorState initStateDetector) {
        dataController.setGrayCircle();
        dataController.setImageViewLabel("", "", "");
    }

    private void handleAccumulationDetectorState(DataController dataController, AccumulationDetectorState accStateDetector) {
        dataController.setYellowCircle();
        dataController.setImageViewLabel(100 * accStateDetector.curTime / accStateDetector.measTime + "%", "", "");
    }

    private void handleInternalEvent(DataController dataController, InternalEvent internalEvent) {
        log.info("handleInternalEvent time: {}", internalEvent.time);

        if (statisticMeasurements.containsKey(internalEvent.time)) {
            StatisticMeasurement statMeas = statisticMeasurements.get(internalEvent.time);

            statisticMeasService.addMeasToStatistic(internalEvent.time, internalEvent.internalData, statMeas);

            dataController.showStatisticMeas(statMeas);
        } else {
            StatisticMeasurement statMeas = new StatisticMeasurement();
            statisticMeasService.addMeasToStatistic(internalEvent.time, internalEvent.internalData, statMeas);

            statisticMeasurements.put(internalEvent.time, statMeas);
        }
    }

    private void handleMeasDetectorState(DataController dataController, MeasurementDetectorState measStateDetector) {
        log.info("handleMeasDetectorState time: {}", measStateDetector.time);

        dataController.setGreenCircle();
        dataController.setImageViewLabel("", "", "");

        if (statisticMeasurements.containsKey(measStateDetector.time)) {
            StatisticMeasurement statMeas = statisticMeasurements.get(measStateDetector.time);

            statisticMeasService.addMeasToStatistic(measStateDetector.time, measStateDetector.meas, statMeas);

            dataController.showStatisticMeas(statMeas);
        } else {
            StatisticMeasurement statMeas = new StatisticMeasurement();
            statisticMeasService.addMeasToStatistic(measStateDetector.time, measStateDetector.meas, statMeas);

            statisticMeasurements.put(measStateDetector.time, statMeas);
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
                if (isDialogShow(1000))
                    managementController.showDialogParamIsSet(COR_COEF);
            } else {
                managementController.showAnswerErrorDialog(COR_COEF, getErrorByCode(detectorMsg.data[0]));
            }

        } else if (detectorMsg instanceof GetCounterCorrectCoeffAnswer answer) {
            if (detectorMsg.commandStatusCode == SUCCESS) {
                managementController.setCounterCorrectCoeff(answer.counterIndex, answer.counterCorrectCoeff);

                if (isDialogShow(1000))
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

    private long showDialogLastTime = 0;

    private boolean isDialogShow(long deltaMillis) {
        long showDialogCurrentTime = System.currentTimeMillis();
        boolean res = showDialogLastTime == 0 || showDialogCurrentTime - showDialogLastTime > deltaMillis;
        showDialogLastTime = showDialogCurrentTime;
        return res;
    }

    public void startMeasurement() {
        statisticMeasService.clear();
        executorService.submit(() -> {
            try {
                detectorClient.connect();
            } catch (Exception e) {
                log.error("detector client connect exception", e);
            }
        });

        connectTimer = executorService.submit(() -> {
            DataController dataController = (DataController) getChild(DataController.class.getSimpleName());
            time.set(System.currentTimeMillis());

            try {
                do {
                    Thread.sleep(1000);

                    long cur = System.currentTimeMillis();
                    if (cur - time.get() > 5000) {
                        dataController.setNoConnect();
                    }
                } while (!Thread.currentThread().isInterrupted());
                log.info("timer canceled");
            } catch (Exception e) {
                log.error("timer connect exception", e);
            }
        });
    }

    public void stopMeasurement() {
        connectTimer.cancel(true);

        detectorClient.close();
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

    public void saveMeasurements(File file, String fileComment) {
        DoubleProperty progressProperty = new SimpleDoubleProperty(0.0);
        StringProperty statusProperty = new SimpleStringProperty("");

        final ProgressDialog progressDialog = mainWindow.showProgressDialog(progressProperty, statusProperty);

        executorService.submit(() -> {
            log.info("export to selected file {}", file);

            try {
                exportService.exportMeasurements(statisticMeasurements.values(), fileComment, file, (n, progress) ->
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
            } catch (Exception e) {
                log.error("saveMeasurements", e);
            }
        });


    }

    public void saveDetectorMessages(File file) {
        DoubleProperty progressProperty = new SimpleDoubleProperty(0.0);
        StringProperty statusProperty = new SimpleStringProperty("");

        final ProgressDialog progressDialog = mainWindow.showProgressDialog(progressProperty, statusProperty);

        log.info("export to selected file {}", file);

        try {
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
        } catch (Exception e) {
            log.error("saveMeasurements", e);
        }
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

        try {
            detectorClient.close();
        } catch (Exception e) {
            log.error("detector client shutdown error", e);
        }

        executorService.shutdownNow();
        log.info("scheduledExecutorService shutdown OK");

        try {
            boolean res = executorService.awaitTermination(500, TimeUnit.MILLISECONDS);
            log.info("executorService is terminated: {}", res);
        } catch (InterruptedException e) {
            log.error("executorService.awaitTermination", e);
        }
    }
}
