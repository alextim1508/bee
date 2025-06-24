package com.alextim.bee;


import com.alextim.bee.client.DetectorClientAbstract;
import com.alextim.bee.client.dto.GeoData;
import com.alextim.bee.client.messages.DetectorCommands.*;
import com.alextim.bee.client.messages.DetectorMsg;
import com.alextim.bee.client.protocol.DetectorCodes.AttentionFlags;
import com.alextim.bee.client.protocol.DetectorCodes.BDInternalMode;
import com.alextim.bee.context.AppState;
import com.alextim.bee.frontend.MainWindow;
import com.alextim.bee.frontend.dialog.progress.ProgressDialog;
import com.alextim.bee.frontend.view.NodeController;
import com.alextim.bee.frontend.view.data.DataController;
import com.alextim.bee.frontend.view.magazine.MagazineController;
import com.alextim.bee.frontend.view.management.ManagementController;
import com.alextim.bee.frontend.view.metrology.MetrologyController;
import com.alextim.bee.frontend.view.setting.SettingController;
import com.alextim.bee.service.AccumulationMeasService;
import com.alextim.bee.service.AccumulationMeasService.AccumulatedMeasurement;
import com.alextim.bee.service.ExportService;
import com.alextim.bee.service.MetrologyMeasService;
import com.alextim.bee.service.MetrologyMeasService.MetrologyMeasurement;
import com.alextim.bee.service.StatisticMeasService;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

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
                          MetrologyMeasService metrologyMeasService,
                          AccumulationMeasService accumulationMeasService,
                          ExportService exportService) {
        super(appState,
                mainWindow,
                detectorClient,
                statisticMeasService,
                metrologyMeasService,
                accumulationMeasService,
                exportService);
    }

    private final ConcurrentLinkedQueue<DetectorMsg> detectorMsgs = new ConcurrentLinkedQueue<>();

    private final ConcurrentLinkedQueue<StatisticMeasurement> statisticMsg = new ConcurrentLinkedQueue<>();
    private final Map<Long, StatisticMeasurement> statisticMsgMap = new HashMap<>();

    private final Map<Class<? extends SomeCommandAnswer>, List<SomeCommand>> waitingCommands = new HashMap<>();
    private final Map<Class<? extends SomeCommandAnswer>, Class<? extends NodeController>> nodeControllerReceiver = new HashMap<>();

    protected final ExecutorService executorService = Executors.newCachedThreadPool();

    private Future<?> connectTimer;
    private Future<?> geoDataSender;
    private final AtomicLong lastReceivedMsgTime = new AtomicLong();

    private Future<?> secretCoefShowTimer;
    private final AtomicLong secretCoefUpdatedTime = new AtomicLong();
    private final AtomicReference<Float> secretCoef = new AtomicReference<>(1.0f);

    @SneakyThrows
    public void listenDetectorClient() {
        Runnable task = () -> {

            DataController dataController = (DataController) getChild(DataController.class.getSimpleName());
            MagazineController magazineController = (MagazineController) getChild(MagazineController.class.getSimpleName());
            ManagementController managementController = (ManagementController) getChild(ManagementController.class.getSimpleName());
            MetrologyController metrologyController = (MetrologyController) getChild(MetrologyController.class.getSimpleName());
            SettingController settingController = (SettingController) getChild(SettingController.class.getSimpleName());

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
                log.info("DetectorMsg: {}" + System.lineSeparator() + "{}", detectorMsg.getClass().getSimpleName(), detectorMsg);

                magazineController.addLog(detectorMsg);

                addDetectorMsg(detectorMsg);

                if (detectorMsg instanceof SomeEvent event) {
                    try {
                        handleEvent(dataController, metrologyController, managementController, event);
                    } catch (Exception e) {
                        log.error("handleEvent exception", e);
                    }

                } else if (detectorMsg instanceof SomeCommandAnswer answer) {
                    try {
                        handleCommandAnswer(dataController, managementController, settingController, answer);
                    } catch (Exception e) {
                        log.error("handleCommandAnswer exception", e);
                    }
                }


            }
            log.info("queue handle task is done");
        };

        executorService.submit(task);
    }

    private void handleEvent(DataController dataController,
                             MetrologyController metrologyController,
                             ManagementController managementController,
                             SomeEvent detectorMsg) {
        if (detectorMsg instanceof RestartDetectorState restartDetectorState) {
            handleRestartDetectorState(managementController, restartDetectorState);

        } else if (detectorMsg instanceof InitializationDetectorState state) {
            handleInitializationDetectorState(dataController, state);

        } else if (detectorMsg instanceof AccumulationDetectorState state) {
            handleAccumulationDetectorState(dataController, state);

        } else if (detectorMsg instanceof MeasurementDetectorState state) {
            handleMeasDetectorState(dataController, metrologyController, state);

        } else if (detectorMsg instanceof InternalEvent event) {
            handleInternalEvent(dataController, metrologyController, event);

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
        dataController.setImageViewLabel(String.format("0x%x", errorDetectorState.error.code), "");
    }

    private void handleInitializationDetectorState(DataController dataController, InitializationDetectorState initStateDetector) {
        if (areThereAttentionFlags(initStateDetector.attentionFlags)) {
            dataController.setGrayCircle();
        } else {
            dataController.setGrayCircleExclamation();
        }
        dataController.setImageViewLabel("", "");
    }

    private void handleAccumulationDetectorState(DataController dataController, AccumulationDetectorState accStateDetector) {
        if (areThereAttentionFlags(accStateDetector.attentionFlags)) {
            dataController.setYellowCircle();
        } else {
            dataController.setYellowCircleExclamation();
        }
        dataController.setImageViewLabel(100 * accStateDetector.curTime / accStateDetector.measTime + "%", "");
    }

    private void handleMeasDetectorState(DataController dataController,
                                         MetrologyController metrologyController,
                                         MeasurementDetectorState measStateDetector) {
        log.info("handleMeasDetectorState. detector time: {}", measStateDetector.time);

        if (areThereAttentionFlags(measStateDetector.attentionFlags)) {
            dataController.setGreenCircle();
        } else {
            dataController.setGreenCircleExclamation();
        }
        dataController.setImageViewLabel("", "");

        if (statisticMsgMap.containsKey(measStateDetector.time)) {
            StatisticMeasurement statMeas = statisticMsgMap.remove(measStateDetector.time);

            statisticMeasService.addMeasToStatistic(measStateDetector.time, measStateDetector.meas, secretCoef.get(), statMeas);
            dataController.showStatisticMeas(statMeas);

            handleMetrology(metrologyController, statMeas);
            handleAccumulation(dataController, statMeas);

            addStatisticMsg(statMeas);
        } else {
            StatisticMeasurement statMeas = new StatisticMeasurement();
            statisticMeasService.addMeasToStatistic(measStateDetector.time, measStateDetector.meas, secretCoef.get(), statMeas);

            statisticMsgMap.put(measStateDetector.time, statMeas);
        }
    }

    private void handleInternalEvent(DataController dataController,
                                     MetrologyController metrologyController,
                                     InternalEvent internalEvent) {
        log.info("handleInternalEvent. detector time: {}", internalEvent.time);

        if (statisticMsgMap.containsKey(internalEvent.time)) {
            StatisticMeasurement statMeas = statisticMsgMap.remove(internalEvent.time);

            statisticMeasService.addMeasToStatistic(internalEvent.time, internalEvent.internalData, statMeas);

            dataController.showStatisticMeas(statMeas);

            handleMetrology(metrologyController, statMeas);
            handleAccumulation(dataController, statMeas);

            addStatisticMsg(statMeas);
        } else {
            StatisticMeasurement statMeas = new StatisticMeasurement();
            statisticMeasService.addMeasToStatistic(internalEvent.time, internalEvent.internalData, statMeas);

            statisticMsgMap.put(internalEvent.time, statMeas);
        }

        if(internalEvent.internalData.mode == BDInternalMode.BD_MODE_COUNTERS_OFF) {
            dataController.setVoltageIcon();
        } else {
            dataController.setEmptyCircle2();
        }
    }

    private void handleAccumulation(DataController dataController, StatisticMeasurement statMeas) {
        if (accumulationMeasService.isRun()) {
            AccumulatedMeasurement accumulatedMeasurement = accumulationMeasService.addMeasToAccumulation(statMeas);
            dataController.showAccumulatedMeas(accumulatedMeasurement);
        }
    }

    private void handleMetrology(MetrologyController metrologyController, StatisticMeasurement statMeas) {
        if (metrologyMeasService.isRun()) {
            MetrologyMeasurement metrologyMeasurement = metrologyMeasService.addMeasToMetrology(statMeas);
            metrologyController.showMetrologyMeas(metrologyMeasurement);
        }
    }

    public boolean areThereAttentionFlags(AttentionFlags attentionFlags) {
        return attentionFlags.equals(new AttentionFlags(Set.of(NO_ATTENTION)));
    }

    private void handleCommandAnswer(DataController dataController,
                                     ManagementController managementController,
                                     SettingController settingController,
                                     SomeCommandAnswer detectorMsg) {
        log.info("handleCommandAnswer: {}", detectorMsg);

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

        } else if (detectorMsg instanceof SetDebugSettingAnswer answer) {
            Class<? extends NodeController> node = nodeControllerReceiver.remove(SetDebugSettingAnswer.class);

            List<SomeCommand> waitingCommand = waitingCommands.getOrDefault(SetDebugSettingAnswer.class, new LinkedList<>());

            if(node == DataController.class) {
                if (detectorMsg.commandStatusCode == SUCCESS) {
                    if (waitingCommand.isEmpty()) {
                        dataController.showDialogModeIsSet();
                    } else {

                        new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    nodeControllerReceiver.put(SetDebugSettingAnswer.class, DataController.class);
                                    sendDetectorCommand(waitingCommand.remove(0));
                                }
                            }, 2000);
                    }
                } else {
                    dataController.showAnswerErrorDialog(getErrorByCode(detectorMsg.data[0]));
                }
            } else {
                if (detectorMsg.commandStatusCode == SUCCESS) {
                    settingController.showDialogParamIsSet(DEBUG_SETTING);
                } else {
                    settingController.showAnswerErrorDialog(DEBUG_SETTING, getErrorByCode(detectorMsg.data[0]));
                }
            }

        } else if (detectorMsg instanceof GetDebugSettingAnswer answer) {
            Class<? extends NodeController> node = nodeControllerReceiver.remove(GetDebugSettingAnswer.class);
            if(node == DataController.class) {
                dataController.handleGetDebugSettings(answer);
            } else {
                if (detectorMsg.commandStatusCode == SUCCESS) {
                    settingController.setDebugSetting(answer.debugSetting);
                    settingController.showDialogParamIsGot(DEBUG_SETTING);
                } else {
                    settingController.showAnswerErrorDialog(DEBUG_SETTING, getErrorByCode(detectorMsg.data[0]));
                }
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

        } else if (detectorMsg instanceof SetImpulseRangeCounterCommandAnswer answer) {
            List<SomeCommand> waitingCommand =
                    waitingCommands.getOrDefault(SetImpulseRangeCounterCommandAnswer.class, new LinkedList<>());

            if (detectorMsg.commandStatusCode == SUCCESS) {
                if (waitingCommand.isEmpty()) {
                    managementController.showDialogParamIsSet(IMPULSE_MODE_RANGE);
                } else {
                    sendDetectorCommand(waitingCommand.remove(0));
                }
            } else {
                waitingCommand.clear();
                managementController.showAnswerErrorDialog(IMPULSE_MODE_RANGE, getErrorByCode(detectorMsg.data[0]));
            }

        } else if (detectorMsg instanceof GetImpulseRangeCounterCommandAnswer answer) {
            List<SomeCommand> waitingCommand =
                    waitingCommands.getOrDefault(GetImpulseRangeCounterCommandAnswer.class, new LinkedList<>());

            if (detectorMsg.commandStatusCode == SUCCESS) {
                managementController.setImpulseModeCounter(answer.counterIndex, answer.impulseRangeCounter);
                if (waitingCommand.isEmpty()) {
                    managementController.showDialogParamIsGot(IMPULSE_MODE_RANGE);
                } else {
                    sendDetectorCommand(waitingCommand.remove(0));
                }
            } else {
                waitingCommand.clear();
                managementController.showAnswerErrorDialog(IMPULSE_MODE_RANGE, getErrorByCode(detectorMsg.data[0]));
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
        statisticMsg.clear();

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
                        sendDetectorCommand(new SetGeoDataCommand(TRANSFER_ID, new GeoData(curLat, curLon)));
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

                        log.error("========== NO CONNECT ==========");
                    }
                } while (!Thread.currentThread().isInterrupted());
                log.info("timer canceled");
            } catch (Exception e) {
                log.error("timer connect exception", e);
            }
        });

        secretCoefShowTimer = executorService.submit(() -> {
            DataController dataController = (DataController) getChild(DataController.class.getSimpleName());
            secretCoefUpdatedTime.set(System.currentTimeMillis());

            try {
                do {
                    Thread.sleep(1000);

                    long cur = System.currentTimeMillis();
                    if (cur - secretCoefUpdatedTime.get() > 1000) {
                        dataController.setSecret("");
                    }
                } while (!Thread.currentThread().isInterrupted());
                log.info("timer2 canceled");
            } catch (Exception e) {
                log.error("timer2 connect exception", e);
            }
        });

        ((ManagementController) getChild(ManagementController.class.getSimpleName())).setDisableAllButtons(false);
        ((MetrologyController) getChild(MetrologyController.class.getSimpleName())).setDisableAllButtons(false);
        ((SettingController) getChild(SettingController.class.getSimpleName())).setDisableAllButtons(false);
    }

    public void stopMeasurement() {
        connectTimer.cancel(true);

        if (GEO_DATA_ENABLE) {
            geoDataSender.cancel(true);
        }

        secretCoefShowTimer.cancel(true);

        detectorClient.close();

        ((DataController) getChild(DataController.class.getSimpleName())).setEmptyCircle1();

        ((ManagementController) getChild(ManagementController.class.getSimpleName())).setDisableAllButtons(true);
        ((MetrologyController) getChild(MetrologyController.class.getSimpleName())).setDisableAllButtons(true);
        ((SettingController) getChild(SettingController.class.getSimpleName())).setDisableAllButtons(true);
    }

    public void startMetrology(int cycleAmount, int measAmount, float realMeasData) {
        metrologyMeasService.run(cycleAmount, measAmount, realMeasData);
    }

    public void startAccumulation(int measAmount) {
        accumulationMeasService.run(measAmount);
    }

    private final KeyCombination ctrlQ = new KeyCodeCombination(KeyCode.Q, KeyCodeCombination.CONTROL_DOWN);
    private final KeyCombination ctrlW = new KeyCodeCombination(KeyCode.W, KeyCodeCombination.CONTROL_DOWN);
    private final KeyCombination ctrlE = new KeyCodeCombination(KeyCode.E, KeyCodeCombination.CONTROL_DOWN);
    private final KeyCombination ctrlR = new KeyCodeCombination(KeyCode.R, KeyCodeCombination.CONTROL_DOWN);

    public void onKeyEvent(KeyEvent event) {
        DataController dataController = (DataController) getChild(DataController.class.getSimpleName());

        if(ctrlQ.match(event)) {
            secretCoefUpdatedTime.set(System.currentTimeMillis());

            dataController.setSecret(String.format(Locale.US, "%.2f",
                    secretCoef.accumulateAndGet(0.2f, Float::sum))
            );
        } else if(ctrlW.match(event)) {
            secretCoefUpdatedTime.set(System.currentTimeMillis());

            dataController.setSecret(String.format(Locale.US, "%.2f",
                    secretCoef.updateAndGet(v -> {
                        if(v > 1) {
                            return v - 0.2f;
                        } else {
                            return v > 0.05f ? v - 0.05f : 0.0f;
                        }
                    }))
            );
        } else if(ctrlE.match(event)) {
            secretCoefUpdatedTime.set(System.currentTimeMillis());

            dataController.setSecret(String.format(Locale.US, "%.0f",
                    secretCoef.updateAndGet(v -> 1.0f))
            );
        } else if(ctrlR.match(event)) {
            secretCoefUpdatedTime.set(System.currentTimeMillis());

            dataController.setSecret("");
            clear();
        }
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

    private void addDetectorMsg(DetectorMsg msg) {
        detectorMsgs.add(msg);
        if(detectorMsgs.size() > QUEUE_CAPACITY)
            detectorMsgs.poll();
    }

    private void addStatisticMsg(StatisticMeasurement msg) {
        statisticMsg.add(msg);
        if(statisticMsg.size() > QUEUE_CAPACITY)
            statisticMsg.poll();
    }

    public void addWaitingCommand(Class<? extends SomeCommandAnswer> cl, SomeCommand command) {
        waitingCommands.putIfAbsent(cl, new LinkedList<>());
        waitingCommands.get(cl).add(command);
    }

    public void addNodeControllerReceiver(Class<? extends SomeCommandAnswer> commandClass,
                                          Class<? extends NodeController> receiverClass) {
        nodeControllerReceiver.put(commandClass, receiverClass);
    }

    public void saveMeasurements(File file, String fileComment) {
        DoubleProperty progressProperty = new SimpleDoubleProperty(0.0);
        StringProperty statusProperty = new SimpleStringProperty("");

        final ProgressDialog progressDialog = mainWindow.showProgressDialog(progressProperty, statusProperty);

        executorService.submit(() -> {
            log.info("export to selected file {}", file);

            try {
                exportService.exportMeasurements(statisticMsg, fileComment, file, (n, progress) ->
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

        executorService.submit(() -> {
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
        });
    }

    public void clear() {
        detectorMsgs.clear();

        statisticMsg.clear();
        statisticMsgMap.clear();

        statisticMeasService.clear();

        DataController dataController = (DataController) getChild(DataController.class.getSimpleName());
        dataController.clearGraphAndTableData();

        MagazineController magazineController = (MagazineController) getChild(MagazineController.class.getSimpleName());
        magazineController.clearTable();
    }

    public void close() {
        log.info("close");

        try {
            ((DataController) getChild(DataController.class.getSimpleName())).putStateParam();
            ((MetrologyController) getChild(MetrologyController.class.getSimpleName())).putStateParam();
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
