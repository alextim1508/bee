package com.alextim.bee.frontend.dialog.error;


import com.alextim.bee.frontend.dialog.AbstractDialogController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class ErrorController extends AbstractDialogController {

    @FXML
    private Label header;
    @FXML
    private TextArea description;

    @Override
    public boolean check() {
        return true;
    }

    public void setHeader(String text) {
        header.setText(text);
    }

    public void setDescription(String text) {
        description.setText(text);
    }

    public Boolean getResult() {
        return true;
    }
}
