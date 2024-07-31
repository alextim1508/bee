package com.alextim.bee.frontend.dialog.progress;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import lombok.SneakyThrows;

public class ProgressDialog extends Dialog<Void> {

    private final String FXML_FILE = "progress.fxml";
    private final String PROGRESS_TITLE = "Долгая операция";

    public ProgressDialog(DoubleProperty doubleProperty, StringProperty stringProperty) {
        init(doubleProperty, stringProperty);
        setTitle(PROGRESS_TITLE);
    }

    @SneakyThrows
    private void init(DoubleProperty doubleProperty, StringProperty stringProperty) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_FILE));
        getDialogPane().setContent(loader.load());

        ProgressController controller = loader.getController();
        controller.bindProperty(doubleProperty, stringProperty);
    }

    public void forcefullyHideDialog() {
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        hide();
        getDialogPane().getButtonTypes().remove(ButtonType.CANCEL);
    }
}