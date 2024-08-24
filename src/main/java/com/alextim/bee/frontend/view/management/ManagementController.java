package com.alextim.bee.frontend.view.management;

import com.alextim.bee.client.messages.DetectorCommands.*;
import com.alextim.bee.client.protocol.DetectorCodes;
import com.alextim.bee.client.protocol.DetectorCodes.BDParam;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

import static com.alextim.bee.client.protocol.DetectorCodes.BDParam.*;
import static com.alextim.bee.client.protocol.DetectorCodes.BDType.GAMMA;
import static com.alextim.bee.context.Property.*;

@Slf4j
public class ManagementController extends ManagementControllerInitializer {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        setSoftwareVersion(SOFTWARE_VERSION);
    }

    @FXML
    void setMeasTimeOn(ActionEvent event) {
        BDParam bdParam = MEAS_TIME;
        if (areYouSure(bdParam)) {
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
        rootController.sendDetectorCommand(new GetSensitivityCommand(TRANSFER_TO_DETECTOR_ID));
    }

    @FXML
    void setDeadTimeOn(ActionEvent event) {
        BDParam bdParam = DEAD_TIME;
        if (areYouSure(bdParam)) {
            try {
                float deadTime = Float.parseFloat(this.deadTime.getText());
                log.info("setDeadTime: {}", deadTime);
                rootController.sendDetectorCommand(new SetDeadTimeCommand(TRANSFER_TO_DETECTOR_ID, deadTime));
            } catch (Exception e) {
                showParsingErrorDialog(bdParam);
            }
        }
    }
    @FXML
    void getDeadTimeOn(ActionEvent event) {
        log.info("getDeadTime");
        rootController.sendDetectorCommand(new GetDeadTimeCommand(TRANSFER_TO_DETECTOR_ID));
    }

    @FXML
    void setCorrCoefOn(ActionEvent event) {
        BDParam bdParam = COR_COEF;
        if (areYouSure(bdParam)) {
            try {
                if (DetectorCodes.BDType.getBDTypeByCode(FRONTEND_FOR_DETECTOR) == GAMMA) {
                    float counter1 = Float.parseFloat(this.counter1.getText());
                    float counter2 = Float.parseFloat(this.counter2.getText());
                    log.info("setCorrCoef: {} {}", counter1, counter2);

                    rootController.sendDetectorCommand(new SetCounterCorrectCoeffCommand(TRANSFER_TO_DETECTOR_ID, 1, counter1));
                    rootController.sendDetectorCommand(new SetCounterCorrectCoeffCommand(TRANSFER_TO_DETECTOR_ID, 2, counter2));

                } else {
                    float counter1 = Float.parseFloat(this.counter1.getText());
                    float counter2 = Float.parseFloat(this.counter2.getText());
                    float counter3 = Float.parseFloat(this.counter3.getText());
                    float counter4 = Float.parseFloat(this.counter4.getText());
                    log.info("setCorrCoef: {} {} {} {}", counter1, counter2, counter3, counter4);

                    rootController.sendDetectorCommand(new SetCounterCorrectCoeffCommand(TRANSFER_TO_DETECTOR_ID, 1, counter1));
                    rootController.sendDetectorCommand(new SetCounterCorrectCoeffCommand(TRANSFER_TO_DETECTOR_ID, 2, counter2));
                    rootController.sendDetectorCommand(new SetCounterCorrectCoeffCommand(TRANSFER_TO_DETECTOR_ID, 3, counter3));
                    rootController.sendDetectorCommand(new SetCounterCorrectCoeffCommand(TRANSFER_TO_DETECTOR_ID, 4, counter4));
                }

            } catch (Exception e) {
                showParsingErrorDialog(bdParam);
            }
        }
    }
    @FXML
    void getCorrCoefOn(ActionEvent event) {
        log.info("getCorrCoef");

        if (DetectorCodes.BDType.getBDTypeByCode(FRONTEND_FOR_DETECTOR) == GAMMA) {
            rootController.sendDetectorCommand(new GetCounterCorrectCoeffCommand(TRANSFER_TO_DETECTOR_ID, 1));
            rootController.sendDetectorCommand(new GetCounterCorrectCoeffCommand(TRANSFER_TO_DETECTOR_ID, 2));
        } else {
            rootController.sendDetectorCommand(new GetCounterCorrectCoeffCommand(TRANSFER_TO_DETECTOR_ID, 1));
            rootController.sendDetectorCommand(new GetCounterCorrectCoeffCommand(TRANSFER_TO_DETECTOR_ID, 2));
            rootController.sendDetectorCommand(new GetCounterCorrectCoeffCommand(TRANSFER_TO_DETECTOR_ID, 3));
            rootController.sendDetectorCommand(new GetCounterCorrectCoeffCommand(TRANSFER_TO_DETECTOR_ID, 4));
        }
    }

    @FXML
    void setGeoDataOn(ActionEvent event) {

    }
    @FXML
    void getGeoDataOn(ActionEvent event) {

    }

    @FXML
    void setIpOn(ActionEvent event) {
        BDParam bdParam = IP_ADDRESS_PORT;
        if (areYouSure(bdParam)) {
            try {
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
            rootController.sendDetectorCommand(new RestartDetectorCommand(TRANSFER_TO_DETECTOR_ID));
        }
    }

    @FXML
    void getVersionHardwareOn(ActionEvent event) {
        log.info("getVersionHardware");
        rootController.sendDetectorCommand(new GetVersionCommand(TRANSFER_TO_DETECTOR_ID));
    }
}
