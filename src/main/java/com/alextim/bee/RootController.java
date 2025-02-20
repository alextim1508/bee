package com.alextim.bee;


import com.alextim.bee.client.DetectorClientAbstract;
import com.alextim.bee.client.dto.GeoData;
import com.alextim.bee.client.messages.DetectorCommands.*;
import com.alextim.bee.client.messages.DetectorMsg;
import com.alextim.bee.client.protocol.DetectorCodes.AttentionFlags;
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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.alextim.bee.client.messages.DetectorEvents.*;
import static com.alextim.bee.client.protocol.DetectorCodes.AttentionFlag.NO_ATTENTION;
import static com.alextim.bee.client.protocol.DetectorCodes.BDParam.*;
import static com.alextim.bee.client.protocol.DetectorCodes.CommandStatus.SUCCESS;
import static com.alextim.bee.client.protocol.DetectorCodes.Error.getErrorByCode;
import static com.alextim.bee.client.protocol.DetectorCodes.RestartReason.RESTART_COMMAND;
import static com.alextim.bee.client.transfer.DetectorParser.parse;
import static com.alextim.bee.context.Property.*;
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

    private final List<DetectorMsg> detectorMsgs =  new CopyOnWriteArrayList<>();

    private final Map<Class<? extends SomeCommandAnswer>, List<SomeCommand>> waitingCommands = new HashMap<>();

    private final Map<Long, StatisticMeasurement> statisticMeasurements = new ConcurrentHashMap<>();

    protected final ExecutorService executorService = Executors.newCachedThreadPool();

    private Future<?> connectTimer;
    private Future<?> geoDataSender;
    private final AtomicLong lastReceivedMsgTime = new AtomicLong();

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

                lastReceivedMsgTime.set(System.currentTimeMillis());

                DetectorMsg detectorMsg = parse(msg);
                log.info("DetectorMsg: {}. {}", detectorMsg.getClass().getSimpleName(), detectorMsg);

                magazineController.addLog(detectorMsg);

                addDetectorMsg(detectorMsg);

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

    private void handleErrorDetectorState(DataController dataController, ErrorDetectorState errorDetectorState) {
        if (areThereAttentionFlags(errorDetectorState.attentionFlags)) {
            dataController.setRedCircle();
        } else {
            dataController.setRedCircleExclamation();
        }
        dataController.setImageViewLabel(String.format("0x%x", errorDetectorState.error.code), "", "");
    }

    private void handleInitializationDetectorState(DataController dataController, InitializationDetectorState initStateDetector) {
        if (areThereAttentionFlags(initStateDetector.attentionFlags)) {
            dataController.setGrayCircle();
        } else {
            dataController.setGrayCircleExclamation();
        }
        dataController.setImageViewLabel("", "", "");
    }

    private void handleAccumulationDetectorState(DataController dataController, AccumulationDetectorState accStateDetector) {
        if (areThereAttentionFlags(accStateDetector.attentionFlags)) {
            dataController.setYellowCircle();
        } else {
            dataController.setYellowCircleExclamation();
        }
        dataController.setImageViewLabel(100 * accStateDetector.curTime / accStateDetector.measTime + "%", "", "");
    }

    private void handleMeasDetectorState(DataController dataController, MeasurementDetectorState measStateDetector) {
        log.info("handleMeasDetectorState time: {}", measStateDetector.time);

        if (areThereAttentionFlags(measStateDetector.attentionFlags)) {
            dataController.setGreenCircle();
        } else {
            dataController.setGreenCircleExclamation();
        }
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

    public boolean areThereAttentionFlags(AttentionFlags attentionFlags) {
        return attentionFlags.equals(new AttentionFlags(Set.of(NO_ATTENTION)));
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
            List<SomeCommand> waitingCommand = waitingCommands.getOrDefault(SetDeadTimeAnswer.class, new LinkedList<>());

            if (detectorMsg.commandStatusCode == SUCCESS) {
                if (waitingCommand.isEmpty()) {
                    managementController.showDialogParamIsSet(DEAD_TIME);
                } else {
                    sendDetectorCommand(waitingCommand.remove(0));
                }
            } else {
                waitingCommand.clear();
                managementController.showAnswerErrorDialog(DEAD_TIME, getErrorByCode(detectorMsg.data[0]));
            }

        } else if (detectorMsg instanceof GetDeadTimeAnswer answer) {
            List<SomeCommand> waitingCommand = waitingCommands.getOrDefault(GetDeadTimeAnswer.class, new LinkedList<>());

            if (detectorMsg.commandStatusCode == SUCCESS) {
                managementController.setDeadTime(answer.counterIndex, answer.mode, answer.deadTime);
                if (waitingCommand.isEmpty()) {
                    managementController.showDialogParamIsGot(DEAD_TIME);
                } else {
                    sendDetectorCommand(waitingCommand.remove(0));
                }
            } else {
                waitingCommand.clear();
                managementController.showAnswerErrorDialog(DEAD_TIME, getErrorByCode(detectorMsg.data[0]));
            }

        } else if (detectorMsg instanceof SetCounterCorrectCoeffAnswer answer) {
            List<SomeCommand> waitingCommand = waitingCommands.getOrDefault(SetCounterCorrectCoeffAnswer.class, new LinkedList<>());

            if (detectorMsg.commandStatusCode == SUCCESS) {
                if (waitingCommand.isEmpty()) {
                    managementController.showDialogParamIsSet(COR_COEF);
                } else {
                    sendDetectorCommand(waitingCommand.remove(0));
                }
            } else {
                waitingCommand.clear();
                managementController.showAnswerErrorDialog(COR_COEF, getErrorByCode(detectorMsg.data[0]));
            }

        } else if (detectorMsg instanceof GetCounterCorrectCoeffAnswer answer) {
            List<SomeCommand> waitingCommand = waitingCommands.getOrDefault(GetCounterCorrectCoeffAnswer.class, new LinkedList<>());

            if (detectorMsg.commandStatusCode == SUCCESS) {
                managementController.setCounterCorrectCoeff(answer.counterIndex, answer.mode, answer.counterCorrectCoeff);

                if (waitingCommand.isEmpty()) {
                    managementController.showDialogParamIsGot(COR_COEF);
                } else {
                    sendDetectorCommand(waitingCommand.remove(0));
                }
            } else {
                waitingCommand.clear();
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

    public void startMeasurement() {
        statisticMeasService.clear();
        executorService.submit(() -> {
            try {
                detectorClient.connect();
            } catch (Exception e) {
                log.error("detector client connect exception", e);
            }
        });

        if (GEO_DATA_ENABLE) {
            geoDataSender = executorService.submit(() -> {
                try {
                    float curLat = GEO_DATA_START_LAT, curLon = GEO_DATA_START_LON;
                    do {
                        Thread.sleep(GEO_DATA_DELAY);

                        curLat += GEO_DATA_DELTA;
                        curLon += GEO_DATA_DELTA;

                        log.info("Send geo data: {} {}", curLat, curLon);
                        sendDetectorCommand(new SetGeoDataCommand(TRANSFER_TO_DETECTOR_ID, new GeoData(curLat, curLon)));
                    } while (!Thread.currentThread().isInterrupted());
                    log.info("geoDataSender canceled");
                } catch (Exception e) {
                    log.error("geoDataSender exception", e);
                }
            });
        }

        connectTimer = executorService.submit(() -> {
            DataController dataController = (DataController) getChild(DataController.class.getSimpleName());
            lastReceivedMsgTime.set(System.currentTimeMillis());

            try {
                do {
                    Thread.sleep(1000);

                    long cur = System.currentTimeMillis();
                    if (cur - lastReceivedMsgTime.get() > 5000) {
                        dataController.setNoConnect();
                    }
                } while (!Thread.currentThread().isInterrupted());
                log.info("timer canceled");
            } catch (Exception e) {
                log.error("timer connect exception", e);
            }
        });

        ((ManagementController) getChild(ManagementController.class.getSimpleName())).setDisableAllButtons(false);
    }

    public void stopMeasurement() {
        connectTimer.cancel(true);

        if (GEO_DATA_ENABLE) {
            geoDataSender.cancel(true);
        }

        detectorClient.close();

        ((ManagementController) getChild(ManagementController.class.getSimpleName())).setDisableAllButtons(true);
    }

    public void sendDetectorCommand(SomeCommand command) {
        log.info("=========== SEND DATA ===========");

        StringBuilder str = new StringBuilder();
        for (int i = 0; i < command.data.length; i++)
            str.append(String.format("%x ", command.data[i]));
        log.info(str.toString());

        ((MagazineController) getChild(MagazineController.class.getSimpleName())).addLog(command);

        addDetectorMsg(command);

        detectorClient.sendCommand(command);
    }

    private synchronized void addDetectorMsg(DetectorMsg msg) {
        detectorMsgs.add(msg);
    }

    public void addWaitingCommand(Class<? extends SomeCommandAnswer> cl, SomeCommand command) {
        waitingCommands.putIfAbsent(cl, new LinkedList<>());
        waitingCommands.get(cl).add(command);
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
                log.error("saveMeasurements error", e);

                Platform.runLater(() -> {
                    progressDialog.forcefullyHideDialog();

                    mainWindow.showError(e);
                });
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
            log.error("saveMessages", e);

            Platform.runLater(() -> {
                progressDialog.forcefullyHideDialog();
                mainWindow.showError(e);
            });
        }
    }

    public void clear() {
        statisticMeasurements.clear();
        detectorMsgs.clear();

        MagazineController magazineController = (MagazineController) getChild(MagazineController.class.getSimpleName());
        magazineController.clear();
    }

    public void close() {
        log.info("close");

        try {
            ((DataController) getChild(DataController.class.getSimpleName())).putStateParam();
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
