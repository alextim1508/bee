package com.alextim.bee.frontend.view.setting;

import com.alextim.bee.client.dto.DebugSetting;
import com.alextim.bee.client.messages.DetectorCommands.GetDebugSettingCommand;
import com.alextim.bee.client.messages.DetectorCommands.SetDebugSettingCommand;
import com.alextim.bee.client.protocol.DetectorCodes.BDInternalMode;
import com.alextim.bee.client.protocol.DetectorCodes.BDParam;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

import static com.alextim.bee.client.protocol.DetectorCodes.BDParam.DEBUG_SETTING;
import static com.alextim.bee.context.Property.TRANSFER_TO_DETECTOR_ID;

@Slf4j
public class SettingController extends SettingControllerInitializer {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
    }

    @FXML
    void setDebugSettingOn(ActionEvent event) {
        BDParam bdParam = DEBUG_SETTING;
        if (areYouSure(bdParam)) {
            try {
                log.info("setDebugSetting");

                BDInternalMode mode = getSelectedMode();
                long chmQuench = Long.parseLong(this.chmQuench.getText());
                long clmQuench = Long.parseLong(this.clmQuench.getText());
                long pmInterval = Long.parseLong(this.pmInterval.getText());
                long pmQuench = Long.parseLong(this.pmQuench.getText());
                long pmHiUp = Long.parseLong(this.pmHiUp.getText());

                DebugSetting debugSetting = DebugSetting.builder()
                        .mode(mode)
                        .chmQuench(chmQuench)
                        .clmQuench(clmQuench)
                        .pmInterval(pmInterval)
                        .pmQuench(pmQuench)
                        .pmHiUp(pmHiUp)
                        .build();

                log.info("debugSetting: {}", debugSetting);

                rootController.sendDetectorCommand(
                        new SetDebugSettingCommand(TRANSFER_TO_DETECTOR_ID, debugSetting));

            } catch (Exception e) {
                log.error("setDebugSettingOn: ", e);
                showParsingErrorDialog(bdParam);
            }
        }
    }

    @FXML
    void getDebugSettingOn(ActionEvent event) {
        log.info("getDebugSetting");

        rootController.sendDetectorCommand(
                new GetDebugSettingCommand(TRANSFER_TO_DETECTOR_ID));
    }
}
