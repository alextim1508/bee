package com.alextim.bee.frontend.view.management;

import com.alextim.bee.client.protocol.DetectorCodes;
import com.alextim.bee.client.protocol.DetectorCodes.BDParam;
import com.alextim.bee.frontend.view.NodeController;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

import static com.alextim.bee.client.protocol.DetectorCodes.BDType.GAMMA;
import static com.alextim.bee.context.Context.DETECTOR_NAME;
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
    public static final String DETECTOR_IS_RESTARTED = "БД успешно перезапущен";

    @FXML
    private AnchorPane pane;

    @FXML
    protected TextField sensitivity;

    @FXML
    protected TextField deadTime;


    @FXML
    private GridPane counterCoefPane;
    @FXML
    protected TextField counter1, counter2, counter3, counter4;

    @FXML
    protected TextField ipAddress1, ipAddress2, ipAddress3, ipAddress4;
    @FXML
    protected TextField ipPort;
    @FXML
    protected TextField ipPortExternal;

    @FXML
    protected TextField versionHardware;

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
        if(DetectorCodes.BDType.getBDTypeByCode(DETECTOR_NAME) == GAMMA) {
            counterCoefPane.getChildren().removeAll(counter3, counter4);
        }
    }

    public void showDialogParamIsSet(BDParam bdParam) {
        mainWindow.showDialog(INFORMATION, "Информация",
                HEADER,
                String.format(PARAM_IS_SET, bdParam.title));
    }

    public void showDialogParamIsGot(BDParam bdParam) {
        mainWindow.showDialog(INFORMATION, "Информация",
                HEADER,
                String.format(PARAM_IS_GOT, bdParam.title));
    }

    protected boolean areYouSure (BDParam bdParam) {
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

    protected boolean areYouSureDetectorRestart () {
        return mainWindow.showDialog(WARNING, "Внимание",
                HEADER,
                ARE_YOU_SURE_TO_RESTART);
    }

    public void showDialogDetectorIsRestarted() {
        mainWindow.showDialog(INFORMATION, "Информация",
                HEADER,
                DETECTOR_IS_RESTARTED);
    }

    public void setSoftwareVersion(String text) {
        softwareVersion.setText(text);
    }
}
