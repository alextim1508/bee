package com.alextim.bee.frontend.view.management;

import com.alextim.bee.client.messages.DetectorCommands.*;
import com.alextim.bee.client.protocol.DetectorCodes.BDInternalMode;
import com.alextim.bee.client.protocol.DetectorCodes.BDParam;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

import static com.alextim.bee.client.protocol.DetectorCodes.BDParam.*;
import static com.alextim.bee.context.Property.*;

@Slf4j
public class ManagementController extends ManagementControllerInitializer {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        setSoftwareVersion(SOFTWARE_VERSION, DATE_BUILD);
    }

    @FXML
    void setMeasTimeOn(ActionEvent event) {
        BDParam bdParam = MEAS_TIME;
        if (areYouSure(bdParam)) {
            log.info("setMeasTimeOn");

            try {
                int measTime = Integer.parseInt(this.measTime.getText());
                log.info("setMeasTime: {}", measTime);
                rootController.sendDetectorCommand(new SetMeasTimeCommand(TRANSFER_TO_DETECTOR_ID, measTime));
            } catch (Exception e) {
                showParsingErrorDialog(bdParam);
            }
        }
    }

    @FXML
    void setSensitivityOn(ActionEvent event) {
        BDParam bdParam = SENSITIVITY;
        if (areYouSure(bdParam)) {
            try {
                log.info("setSensitivityOn");

                float sensitivity = Float.parseFloat(this.sensitivity.getText());
                log.info("setSensitivity: {}", sensitivity);
                rootController.sendDetectorCommand(new SetSensitivityCommand(TRANSFER_TO_DETECTOR_ID, sensitivity));
            } catch (Exception e) {
                showParsingErrorDialog(bdParam);
            }
        }
    }

    @FXML
    void getSensitivityOn(ActionEvent event) {
        log.info("getSensitivity");
        sensitivity.setText("-");
        rootController.sendDetectorCommand(new GetSensitivityCommand(TRANSFER_TO_DETECTOR_ID));
    }

    @FXML
    void setDeadTimeOn(ActionEvent event) {
        BDParam bdParam = DEAD_TIME;
        if (areYouSure(bdParam)) {
            try {
                log.info("setDeadTimeOn");

                float deadTime1 = Float.parseFloat(this.deadTime1.getText());
                float deadTime2 = Float.parseFloat(this.deadTime2.getText());
                log.info("deadTimes: {} {}", deadTime1, deadTime2);

                BDInternalMode selectedMode = getSelectedMode();
                log.info("selectedMode: {}", selectedMode);

                rootController.addWaitingCommand(
                        SetDeadTimeAnswer.class,
                        new SetDeadTimeCommand(TRANSFER_TO_DETECTOR_ID,1, selectedMode, deadTime2));

                rootController.sendDetectorCommand(
                        new SetDeadTimeCommand(TRANSFER_TO_DETECTOR_ID, 0, selectedMode, deadTime1));

            } catch (Exception e) {
                log.error("setDeadTimeOn: ", e);
                showParsingErrorDialog(bdParam);
            }
        }
    }

    @SneakyThrows
    @FXML
    void getDeadTimeOn(ActionEvent event) {
        log.info("getDeadTime");

        BDInternalMode selectedMode = getSelectedMode();
        log.info("selectedMode: {}", selectedMode);

        deadTime1.setText("-");
        deadTime2.setText("-");

        rootController.addWaitingCommand(
                GetDeadTimeAnswer.class,
                new GetDeadTimeCommand(TRANSFER_TO_DETECTOR_ID, 1, selectedMode));

        rootController.sendDetectorCommand(
                new GetDeadTimeCommand(TRANSFER_TO_DETECTOR_ID, 0, selectedMode));

    }

    @FXML
    void setCorrCoefOn(ActionEvent event) {
        BDParam bdParam = COR_COEF;
        if (areYouSure(bdParam)) {
            try {
                log.info("setCorrCoefOn");

                float counter1 = Float.parseFloat(this.counterCoef1.getText());
                float counter2 = Float.parseFloat(this.counterCoef2.getText());
                log.info("setCorrCoef: {} {}", counter1, counter2);

                BDInternalMode selectedMode = getSelectedMode();
                log.info("selectedMode: {}", selectedMode);

                rootController.addWaitingCommand(
                        SetCounterCorrectCoeffAnswer.class,
                        new SetCounterCorrectCoeffCommand(TRANSFER_TO_DETECTOR_ID, 1, selectedMode, counter2));

                rootController.sendDetectorCommand(
                        new SetCounterCorrectCoeffCommand(TRANSFER_TO_DETECTOR_ID, 0, selectedMode, counter1));

            } catch (Exception e) {
                log.error("setCorrCoefOn: ", e);
                showParsingErrorDialog(bdParam);
            }
        }
    }

    @SneakyThrows
    @FXML
    void getCorrCoefOn(ActionEvent event) {
        log.info("getCorrCoef");

        BDInternalMode selectedMode = getSelectedMode();
        log.info("selectedMode: {}", selectedMode);

        counterCoef1.setText("-");
        counterCoef2.setText("-");

        rootController.addWaitingCommand(
                GetCounterCorrectCoeffAnswer.class,
                new GetCounterCorrectCoeffCommand(TRANSFER_TO_DETECTOR_ID, 1, selectedMode));

        rootController.sendDetectorCommand(
                new GetCounterCorrectCoeffCommand(TRANSFER_TO_DETECTOR_ID, 0, selectedMode));
    }

    @FXML
    void setImpulseRangeCounterOn(ActionEvent event) {
        BDParam bdParam = IMPULSE_MODE_RANGE;
        if (areYouSure(bdParam)) {
            try {
                log.info("setImpulseRangeCounterOn");

                float counter1 = Float.parseFloat(this.impulseRangeCounter1.getText());
                float counter2 = Float.parseFloat(this.impulseRangeCounter2.getText());
                log.info("setImpulseRangeCounter: {} {}", counter1, counter2);

                rootController.addWaitingCommand(
                        SetImpulseRangeCounterCommandAnswer.class,
                        new SetImpulseRangeCounterCommand(TRANSFER_TO_DETECTOR_ID, 0, counter1));

                rootController.sendDetectorCommand(
                        new SetImpulseRangeCounterCommand(TRANSFER_TO_DETECTOR_ID, 1, counter2));

            } catch (Exception e) {
                log.error("setImpulseRangeCounterOn: ", e);
                showParsingErrorDialog(bdParam);
            }
        }
    }

    @FXML
    void getImpulseRangeCounterOn(ActionEvent event) {
        log.info("getImpulseRangeCounter");

        impulseRangeCounter1.setText("-");
        impulseRangeCounter2.setText("-");

        rootController.addWaitingCommand(
                GetImpulseRangeCounterCommandAnswer.class,
                new GetImpulseRangeCounterCommand(TRANSFER_TO_DETECTOR_ID, 0));

        rootController.sendDetectorCommand(
                new GetImpulseRangeCounterCommand(TRANSFER_TO_DETECTOR_ID, 1));
    }

    @FXML
    void setIpOn(ActionEvent event) {
        BDParam bdParam = IP_ADDRESS_PORT;
        if (areYouSure(bdParam)) {
            try {
                log.info("setIpOn");

                int ipAddr1 = Integer.parseInt(this.ipAddress1.getText());
                int ipAddr2 = Integer.parseInt(this.ipAddress2.getText());
                int ipAddr3 = Integer.parseInt(this.ipAddress3.getText());
                int ipAddr4 = Integer.parseInt(this.ipAddress4.getText());
                int[] ipAddress = new int[]{
                        ipAddr1, ipAddr2, ipAddr3, ipAddr4
                };

                int ipPort = Integer.parseInt(this.ipPort.getText());

                int externalDeviceIpPort = Integer.parseInt(this.externalDeviceIpPort.getText());

                log.info("setIp: {} {} {}", ipAddress, ipPort, externalDeviceIpPort);

                rootController.sendDetectorCommand(new ChangeIpCommand(TRANSFER_TO_DETECTOR_ID, ipAddress, ipPort, externalDeviceIpPort));

            } catch (Exception e) {
                showParsingErrorDialog(bdParam);
            }
        }
    }

    @FXML
    void restartOn(ActionEvent event) {
        if (areYouSureDetectorRestart()) {
            log.info("restart");
            ipAddress1.setText("-");
            ipAddress2.setText("-");
            ipAddress3.setText("-");
            ipAddress4.setText("-");
            externalDeviceIpPort.setText("-");
            ipPort.setText("-");
            rootController.sendDetectorCommand(new RestartDetectorCommand(TRANSFER_TO_DETECTOR_ID));
        }
    }

    @FXML
    void getVersionHardwareOn(ActionEvent event) {
        log.info("getVersionHardwareOn");
        versionHardware.setText("-");
        rootController.sendDetectorCommand(new GetVersionCommand(TRANSFER_TO_DETECTOR_ID));
    }
}
