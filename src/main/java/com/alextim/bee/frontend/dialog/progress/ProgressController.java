package com.alextim.bee.frontend.dialog.progress;


import com.alextim.bee.frontend.dialog.AbstractDialogController;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.alextim.bee.frontend.MainWindow.PROGRESS_BAR_COLOR;

public class ProgressController extends AbstractDialogController implements Initializable {

    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressLabel;
    @FXML
    private Label status;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        progressBar.setStyle("-fx-accent: " + PROGRESS_BAR_COLOR);
    }

    public void bindProperty(DoubleProperty progressProp, StringProperty statusProp) {
        progressBar.progressProperty().bind(progressProp);
        progressLabel.textProperty().bind(progressProp.multiply(100).asString(Locale.US, " %.1f%%"));
        status.textProperty().bind(statusProp);
    }

    @Override
    protected boolean check() {
        return true;
    }
}
