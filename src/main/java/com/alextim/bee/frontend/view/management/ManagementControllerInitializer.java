package com.alextim.bee.frontend.view.management;

import com.alextim.bee.client.protocol.DetectorCodes;
import com.alextim.bee.client.protocol.DetectorCodes.BDParam;
import com.alextim.bee.frontend.view.NodeController;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

import static com.alextim.bee.client.protocol.DetectorCodes.BDType.GAMMA;
import static com.alextim.bee.context.Property.FRONTEND_FOR_DETECTOR;
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
    protected TextField sensitivity;

    @FXML
    protected TextField deadTime;

    @FXML
    protected TextField measTime;

    @FXML
    private GridPane counterCoefPane;
    @FXML
    protected TextField counter1, counter2, counter3, counter4;

    @FXML
    protected TextField ipAddress1, ipAddress2, ipAddress3, ipAddress4;
    @FXML
    protected TextField ipPort;
    @FXML
    protected TextField externalDeviceIpPort;

    @FXML
    protected TextArea versionHardware;

    @FXML
    protected TextField geoData;

    @FXML
    protected Label softwareVersion;


    @Override
    protected String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        paneInit();
        initByDetectorName();
    }

    private void paneInit() {
        /* Bug JavaFX. Other tabs of tabPane get ScrollEvent from current tab */
        pane.addEventHandler(ScrollEvent.ANY, Event::consume);
    }

    private void initByDetectorName() {
        if (DetectorCodes.BDType.getBDTypeByName(FRONTEND_FOR_DETECTOR) == GAMMA) {
            counterCoefPane.getChildren().removeAll(counter3, counter4);
        }
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

    public void setSoftwareVersion(String text) {
        Platform.runLater(() -> {
            softwareVersion.setText(text);
        });
    }

    public void setSensitivity(float sensitivity) {
        Platform.runLater(() -> {
            this.sensitivity.setText(String.valueOf(sensitivity));
        });
    }

    public void setDeadTime(float deadTime) {
        Platform.runLater(() -> {
            this.deadTime.setText(String.valueOf(deadTime));
        });
    }

    public void setHardwareVersion(String version) {
        Platform.runLater(() -> {
            this.versionHardware.setText(version);
        });
    }

    public void setCounterCorrectCoeff(long counterIndex, float counterCorrectCoeff) {
        Platform.runLater(() -> {
            if (counterIndex == 1) {
                this.counter1.setText(String.valueOf(counterCorrectCoeff));
            } else if (counterIndex == 2) {
                this.counter2.setText(String.valueOf(counterCorrectCoeff));
            } else if (counterIndex == 3) {
                this.counter3.setText(String.valueOf(counterCorrectCoeff));
            } else if (counterIndex == 4) {
                this.counter4.setText(String.valueOf(counterCorrectCoeff));
            }
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
}
