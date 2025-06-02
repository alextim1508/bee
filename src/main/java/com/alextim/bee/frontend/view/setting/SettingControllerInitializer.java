package com.alextim.bee.frontend.view.setting;

import com.alextim.bee.client.dto.DebugSetting;
import com.alextim.bee.client.protocol.DetectorCodes;
import com.alextim.bee.client.protocol.DetectorCodes.BDInternalMode;
import com.alextim.bee.frontend.view.NodeController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

import static com.alextim.bee.client.protocol.DetectorCodes.BDInternalMode.*;
import static com.alextim.bee.client.protocol.DetectorCodes.BDInternalMode.BD_MODE_PULSE;
import static com.alextim.bee.frontend.view.management.ManagementControllerInitializer.*;
import static javafx.scene.control.Alert.AlertType.*;

public class SettingControllerInitializer  extends NodeController {

    @FXML
    protected TextField clmQuench;
    @FXML
    protected TextField pmHiUp;
    @FXML
    protected TextField chmQuench;
    @FXML
    protected TextField pmQuench;
    @FXML
    protected TextField pmInterval;
    @FXML
    protected CheckBox isDebugEnable;

    @FXML
    protected ToggleGroup modes;
    @FXML
    protected RadioButton highSens, lowSens, pulse, disable;

    @FXML
    private Button getDebugSettingBtn, setDebugSettingBtn;

    @Override
    protected String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
    }

    public void showDialogParamIsSet(DetectorCodes.BDParam bdParam) {
        Platform.runLater(() -> {
            mainWindow.showDialog(INFORMATION, "Информация",
                    HEADER,
                    String.format(PARAM_IS_SET, bdParam.title));
        });
    }

    public void showDialogParamIsGot(DetectorCodes.BDParam bdParam) {
        Platform.runLater(() -> {
            mainWindow.showDialog(INFORMATION, "Информация",
                    HEADER,
                    String.format(PARAM_IS_GOT, bdParam.title));
        });
    }

    protected boolean areYouSure(DetectorCodes.BDParam bdParam) {
        return mainWindow.showDialog(WARNING, "Внимание",
                HEADER,
                String.format(ARE_YOU_SURE, bdParam.title));
    }

    protected void showParsingErrorDialog(DetectorCodes.BDParam bdParam) {
        mainWindow.showDialog(ERROR, "Ошибка",
                String.format(ERROR_PARSE_FILED, bdParam.title),
                ERROR_PARSE_TITLE);
    }

    public void showAnswerErrorDialog(DetectorCodes.BDParam bdParam, DetectorCodes.Error error) {
        mainWindow.showDialog(ERROR, "Ошибка",
                HEADER,
                String.format(ERROR_ANSWER, bdParam.title, error.title));
    }

    public void setDebugSetting(DebugSetting debugSetting) {
        Platform.runLater(() -> {
            setSelectedMode(debugSetting.mode);
            this.isDebugEnable.setSelected(debugSetting.isDebugEnable);
            this.chmQuench.setText(String.valueOf(debugSetting.chmQuench));
            this.clmQuench.setText(String.valueOf(debugSetting.clmQuench));
            this.pmInterval.setText(String.valueOf(debugSetting.pmInterval));
            this.pmQuench.setText(String.valueOf(debugSetting.pmQuench));
            this.pmHiUp.setText(String.valueOf(debugSetting.pmHiUp));
        });
    }

    protected BDInternalMode getSelectedMode() {
        if (modes.getSelectedToggle() == highSens)
            return BD_MODE_CONTINUOUS_HIGH_SENS;
        if (modes.getSelectedToggle() == lowSens)
            return BD_MODE_CONTINUOUS_LOW_SENS;
        if (modes.getSelectedToggle() == pulse)
            return BD_MODE_PULSE;
        if (modes.getSelectedToggle() == disable)
            return BD_MODE_COUNTERS_OFF;
        return null;
    }

    protected void setSelectedMode(BDInternalMode mode) {
        if (BD_MODE_CONTINUOUS_HIGH_SENS == mode)
            modes.selectToggle(highSens);
        if (BD_MODE_CONTINUOUS_LOW_SENS == mode)
            modes.selectToggle(lowSens);
        if (BD_MODE_PULSE == mode)
            modes.selectToggle(pulse);
        if (BD_MODE_COUNTERS_OFF == mode)
            modes.selectToggle(disable);
    }

    public void setDisableAllButtons(boolean b) {
        getDebugSettingBtn.setDisable(b);
        setDebugSettingBtn.setDisable(b);
    }
}
