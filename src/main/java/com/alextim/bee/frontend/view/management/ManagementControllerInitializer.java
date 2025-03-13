package com.alextim.bee.frontend.view.management;

import com.alextim.bee.client.protocol.DetectorCodes;
import com.alextim.bee.client.protocol.DetectorCodes.BDInternalMode;
import com.alextim.bee.client.protocol.DetectorCodes.BDParam;
import com.alextim.bee.frontend.view.NodeController;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.alextim.bee.client.protocol.DetectorCodes.BDInternalMode.*;
import static javafx.scene.control.Alert.AlertType.*;

public abstract class ManagementControllerInitializer extends NodeController {

    public static final String HEADER = "Работа с параметрами БД";
    public static final String ARE_YOU_SURE = "Вы уверены, что хотите задать Параметр %s в БД ?";
    public static final String ERROR_PARSE_FILED = "Ошибка обработки поля %s";
    public static final String ERROR_PARSE_TITLE = "Ошибка преобразования текста в число";
    public static final String PARAM_IS_SET = "Параметр %s задан в БД";
    public static final String PARAM_IS_GOT = "Параметр %s прочитан из БД";
    public static final String ERROR_ANSWER = "Параметр %s не задан. Команда завершилась с ошибкой: %s";
    public static final String ARE_YOU_SURE_TO_RESTART = "Вы уверены, что хотите перезапустить БД ?";
    public static final String DETECTOR_IS_NORMALLY_RESTARTED = "БД успешно перезапущен";
    public static final String DETECTOR_IS_EMERGENCY_RESTARTED = "БД аварийно перезапущен";

    @FXML
    private AnchorPane pane;

    @FXML
    protected Button setSensitivityBtn, getSensitivityBtn;
    @FXML
    protected TextField sensitivity;

    @FXML
    protected Button setImpulseRangeCounterBtn, getImpulseRangeCounterBtn;
    @FXML
    protected TextField impulseRangeCounter1, impulseRangeCounter2;

    @FXML
    private GridPane deadTimePane;
    @FXML
    protected Button setDeadTimeBtn, getDeadTimeBtn;
    @FXML
    protected TextField deadTime1, deadTime2;

    @FXML
    protected Button setMeasTimeBtn;
    @FXML
    protected TextField measTime;

    @FXML
    private GridPane counterCoefPane;
    @FXML
    protected Button setCounterCoefBtn, getCounterCoefBtn;
    @FXML
    protected TextField counterCoef1, counterCoef2;
    @FXML
    protected ToggleGroup modes;
    @FXML
    protected RadioButton highSens, lowSens, pulse;

    @FXML
    protected Button setIpBtn;
    @FXML
    protected TextField ipAddress1, ipAddress2, ipAddress3, ipAddress4;
    @FXML
    protected TextField ipPort;
    @FXML
    protected TextField externalDeviceIpPort;

    @FXML
    protected Button getVersionHardwareBtn;
    @FXML
    protected TextArea versionHardware;

    @FXML
    protected Button restartBtn;

    @FXML
    protected Label softwareVersion, dateBuild;


    @Override
    protected String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        paneInit();
    }

    private void paneInit() {
        /* Bug JavaFX. Other tabs of tabPane get ScrollEvent from current tab */
        pane.addEventHandler(ScrollEvent.ANY, Event::consume);
    }

    public void showDialogParamIsSet(BDParam bdParam) {
        Platform.runLater(() -> {
            mainWindow.showDialog(INFORMATION, "Информация",
                    HEADER,
                    String.format(PARAM_IS_SET, bdParam.title));
        });
    }

    public void showDialogParamIsGot(BDParam bdParam) {
        Platform.runLater(() -> {
            mainWindow.showDialog(INFORMATION, "Информация",
                    HEADER,
                    String.format(PARAM_IS_GOT, bdParam.title));
        });
    }

    protected boolean areYouSure(BDParam bdParam) {
        return mainWindow.showDialog(WARNING, "Внимание",
                HEADER,
                String.format(ARE_YOU_SURE, bdParam.title));
    }

    protected void showParsingErrorDialog(BDParam bdParam) {
        mainWindow.showDialog(ERROR, "Ошибка",
                String.format(ERROR_PARSE_FILED, bdParam.title),
                ERROR_PARSE_TITLE);
    }

    public void showAnswerErrorDialog(BDParam bdParam, DetectorCodes.Error error) {
        mainWindow.showDialog(ERROR, "Ошибка",
                HEADER,
                String.format(ERROR_ANSWER, bdParam.title, error.title));
    }

    protected boolean areYouSureDetectorRestart() {
        return mainWindow.showDialog(WARNING, "Внимание",
                HEADER,
                ARE_YOU_SURE_TO_RESTART);
    }

    public void showDialogDetectorIsNormallyRestarted() {
        Platform.runLater(() -> {
            mainWindow.showDialog(INFORMATION, "Информация",
                    HEADER,
                    DETECTOR_IS_NORMALLY_RESTARTED);
        });
    }

    public void showDialogDetectorIsCrashRestarted(String reason) {
        Platform.runLater(() -> {
            mainWindow.showDialog(INFORMATION, "Информация",
                    HEADER,
                    DETECTOR_IS_EMERGENCY_RESTARTED + ": " + reason);
        });
    }

    public void setSoftwareVersion(String version, String date) {
        Platform.runLater(() -> {
            softwareVersion.setText(version);
            dateBuild.setText(date);
        });
    }

    public void setSensitivity(float sensitivity) {
        Platform.runLater(() -> {
            this.sensitivity.setText(String.valueOf(sensitivity));
        });
    }

    public void setDeadTime(int counterIndex, BDInternalMode selectedMode, float deadTime) {
        Platform.runLater(() -> {
            if (counterIndex == 0) {
                this.deadTime1.setText(String.format(Locale.US, "%f", deadTime));
            } else if (counterIndex == 1) {
                this.deadTime2.setText(String.format(Locale.US, "%f", deadTime));
            }

            setSelectedMode(selectedMode);
        });
    }

    public void setCounterCorrectCoeff(int counterIndex, BDInternalMode selectedMode, float counterCorrectCoeff) {
        Platform.runLater(() -> {
            if (counterIndex == 0) {
                this.counterCoef1.setText(String.valueOf(counterCorrectCoeff));
            } else if (counterIndex == 1) {
                this.counterCoef2.setText(String.valueOf(counterCorrectCoeff));
            }

            setSelectedMode(selectedMode);
        });
    }

    public void setImpulseModeCounter(int counterIndex, float impulseModeCounter) {
        Platform.runLater(() -> {
            if (counterIndex == 0) {
                this.impulseRangeCounter1.setText(String.format(Locale.US, "%f", impulseModeCounter));
            } else if (counterIndex == 1) {
                this.impulseRangeCounter2.setText(String.format(Locale.US, "%f", impulseModeCounter));
            }
        });
    }

    public void setHardwareVersion(String version) {
        Platform.runLater(() -> {
            this.versionHardware.setText(version);
        });
    }

    public void setIpInfo(int[] ipAddr, int ipPort, int externalDeviceIpPort) {
        Platform.runLater(() -> {
            this.ipAddress1.setText(String.valueOf(ipAddr[0]));
            this.ipAddress2.setText(String.valueOf(ipAddr[1]));
            this.ipAddress3.setText(String.valueOf(ipAddr[2]));
            this.ipAddress4.setText(String.valueOf(ipAddr[3]));

            this.ipPort.setText(String.valueOf(ipPort));

            this.externalDeviceIpPort.setText(String.valueOf(externalDeviceIpPort));
        });
    }

    protected BDInternalMode getSelectedMode() {
        if (modes.getSelectedToggle() == highSens)
            return BD_MODE_CONTINUOUS_HIGH_SENS;
        if (modes.getSelectedToggle() == lowSens)
            return BD_MODE_CONTINUOUS_LOW_SENS;
        if (modes.getSelectedToggle() == pulse)
            return BD_MODE_PULSE;
        return null;
    }

    protected void setSelectedMode(BDInternalMode mode) {
        if (BD_MODE_CONTINUOUS_HIGH_SENS == mode)
            modes.selectToggle(highSens);
        if (BD_MODE_CONTINUOUS_LOW_SENS == mode)
            modes.selectToggle(lowSens);
        if (BD_MODE_PULSE == mode)
            modes.selectToggle(pulse);
    }

    public void setDisableAllButtons(boolean b) {
        setSensitivityBtn.setDisable(b);
        getSensitivityBtn.setDisable(b);
        setDeadTimeBtn.setDisable(b);
        getDeadTimeBtn.setDisable(b);
        setMeasTimeBtn.setDisable(b);
        setCounterCoefBtn.setDisable(b);
        getCounterCoefBtn.setDisable(b);
        setImpulseRangeCounterBtn.setDisable(b);
        getImpulseRangeCounterBtn.setDisable(b);
        getVersionHardwareBtn.setDisable(b);
        restartBtn.setDisable(b);
        setIpBtn.setDisable(b);
    }
}
