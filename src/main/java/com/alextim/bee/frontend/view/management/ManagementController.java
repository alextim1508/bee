package com.alextim.bee.frontend.view.management;

import com.alextim.bee.client.protocol.DetectorCodes.BDParam;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.ResourceBundle;

import static com.alextim.bee.client.protocol.DetectorCodes.BDParam.SENSITIVITY;
import static com.alextim.bee.context.Context.SOFTWARE_VERSION;

public class ManagementController extends ManagementControllerInitializer {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        setSoftwareVersion(SOFTWARE_VERSION);
    }

    @FXML
    void setSensitivityOn(ActionEvent event) {
        BDParam bdParam = SENSITIVITY;
        if(areYouSure(bdParam)) {
            try {
                float sensitivity = Float.parseFloat(this.sensitivity.getText());
            } catch (Exception e) {
                showParsingErrorDialog(bdParam);
            }
            showDialogParamIsSet(bdParam);
        }
    }
    @FXML
    void getSensitivityOn(ActionEvent event) {
        showDialogParamIsGot(SENSITIVITY);
    }

    @FXML
    void setDeadTimeOn(ActionEvent event) {

    }
    @FXML
    void getDeadTimeOn(ActionEvent event) {

    }

    @FXML
    void setCorrCoefOn(ActionEvent event) {

    }
    @FXML
    void getCorrCoefOn(ActionEvent event) {

    }

    @FXML
    void setGeoDataOn(ActionEvent event) {

    }
    @FXML
    void getGeoDataOn(ActionEvent event) {

    }

    @FXML
    void setIpOn(ActionEvent event) {

    }

    @FXML
    void restartOn(ActionEvent event) {

    }

    @FXML
    void getVersionHardwareOn(ActionEvent event) {

    }
}
